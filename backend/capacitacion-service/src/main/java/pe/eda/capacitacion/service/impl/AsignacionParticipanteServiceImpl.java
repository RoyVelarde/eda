package pe.eda.capacitacion.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.eda.capacitacion.client.AulaClient;
import pe.eda.capacitacion.client.PersonalClient;
import pe.eda.capacitacion.dto.request.AsignacionParticipanteRequest;
import pe.eda.capacitacion.dto.request.CambiarAsignacionParticipanteRequest;
import pe.eda.capacitacion.dto.response.AsignacionParticipanteResponse;
import pe.eda.capacitacion.dto.response.ResumenIngresosResponse;
import pe.eda.capacitacion.entity.AsignacionParticipante;
import pe.eda.capacitacion.entity.AulaCapacitacion;
import pe.eda.capacitacion.entity.Capacitacion;
import pe.eda.capacitacion.enums.EstadoCapacitacion;
import pe.eda.capacitacion.enums.FuncionParticipante;
import pe.eda.capacitacion.exception.ReglaNegocioException;
import pe.eda.capacitacion.exception.RecursoDuplicadoException;
import pe.eda.capacitacion.exception.RecursoNoEncontradoException;
import pe.eda.capacitacion.mapper.AsignacionParticipanteMapper;
import pe.eda.capacitacion.repository.AsignacionParticipanteRepository;
import pe.eda.capacitacion.repository.AulaCapacitacionRepository;
import pe.eda.capacitacion.repository.CapacitacionRepository;
import pe.eda.capacitacion.service.AsignacionParticipanteService;
import pe.eda.capacitacion.service.CodigoReservaValidator;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AsignacionParticipanteServiceImpl implements AsignacionParticipanteService {
    private final AsignacionParticipanteRepository asignacionRepository;
    private final AulaCapacitacionRepository aulaCapacitacionRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final PersonalClient personalClient;
    private final AulaClient aulaClient;
    private final CodigoReservaValidator codigoReservaValidator;
    private final AsignacionParticipanteMapper mapper;

    @Transactional
    @Override
    public AsignacionParticipanteResponse asignar(
            UUID capacitacionId,
            AsignacionParticipanteRequest request,
            String token
    ) {
        Capacitacion capacitacion = capacitacionRepository
                .findById(capacitacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la capacitación."
                ));
        if (capacitacion.getEstado() != EstadoCapacitacion.EN_CURSO) {
            throw new ReglaNegocioException(
                    "Solo se pueden asignar participantes a capacitaciones en curso."
            );
        }
        PersonalClient.PersonalResponse personal =
                personalClient.buscarPorDni(request.dni(), token);
        if (personal == null || personal.id() == null) {
            throw new RecursoNoEncontradoException(
                    "No se encontró al participante."
            );
        }
        if (!"NIVEL_3".equalsIgnoreCase(personal.nivel())) {
            throw new ReglaNegocioException(
                    "Solo se pueden asignar participantes de Nivel 3."
            );
        }
        if (asignacionRepository.existsByCapacitacion_IdAndPersonalIdAndActivaTrue(
                capacitacionId,
                personal.id()
        )) {
            throw new RecursoDuplicadoException(
                    "El participante ya tiene un aula asignada en esta capacitación."
            );
        }
        String situacion = personal.situacion() == null
                ? ""
                : personal.situacion().trim().toUpperCase();

        if ("RESERVA".equals(situacion)) {
            codigoReservaValidator.validar(
                    capacitacionId,
                    request.codigoReserva()
            );
        } else if (!"TITULAR".equals(situacion)) {
            throw new ReglaNegocioException(
                    "La situación del participante no permite la asignación."
            );
        }
        FuncionParticipante funcion =
                FuncionParticipante.desdeTexto(personal.cargo());
        List<AulaCapacitacion> aulas = aulaCapacitacionRepository
                .findAllByCapacitacion_IdAndActivaTrueOrderByFechaAsignacionAsc(
                        capacitacionId
                );
        for (AulaCapacitacion aulaCapacitacion : aulas) {
            AulaCapacitacion bloqueada = aulaCapacitacionRepository
                    .buscarActivaPorIdConBloqueo(aulaCapacitacion.getId())
                    .orElse(null);
            if (bloqueada == null) {
                continue;
            }
            AulaClient.AulaResponse aula = aulaClient.buscarPorId(bloqueada.getAulaId(), token);
            if (aula == null
                    || !Boolean.TRUE.equals(aula.activa())
                    || aula.capacidad() == null
                    || aula.capacidad() <= 0) {
                continue;
            }
            if (!funcion.name().equalsIgnoreCase(aula.tipo())) {
                continue;
            }
            long ocupacion = asignacionRepository
                    .countByAulaCapacitacion_IdAndActivaTrue(bloqueada.getId());
            if (ocupacion >= aula.capacidad()) {
                continue;
            }
            AsignacionParticipante asignacion = new AsignacionParticipante();
            asignacion.setCapacitacion(capacitacion);
            asignacion.setAulaCapacitacion(bloqueada);
            asignacion.setPersonalId(personal.id());
            asignacion.setFuncion(funcion.name());
            asignacion.setActiva(true);
            AsignacionParticipante guardada = asignacionRepository.save(asignacion);
            return mapper.toResponse(guardada, personal, aula);
        }
        throw new ReglaNegocioException(
                "No hay aulas activas con capacidad disponible para la función "
                        + funcion.name() + "."
        );
    }

    @Transactional
    @Override
    public AsignacionParticipanteResponse cambiarAsignacion(
            UUID capacitacionId,
            UUID asignacionId,
            CambiarAsignacionParticipanteRequest request,
            String token
    ) {
        Capacitacion capacitacion = capacitacionRepository
                .findById(capacitacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la capacitación."
                ));
        if (capacitacion.getEstado() != EstadoCapacitacion.EN_CURSO) {
            throw new ReglaNegocioException(
                    "Solo se pueden modificar asignaciones de capacitaciones en curso."
            );
        }
        AsignacionParticipante asignacion = asignacionRepository
                .findById(asignacionId)
                .filter(a -> Boolean.TRUE.equals(a.getActiva()))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró una asignación activa con el ID indicado."
                ));
        if (!asignacion.getCapacitacion().getId().equals(capacitacionId)) {
            throw new ReglaNegocioException(
                    "La asignación no pertenece a esta capacitación."
            );
        }
        FuncionParticipante funcion = FuncionParticipante.desdeTexto(
                request.funcion()
        );
        AulaCapacitacion aulaDestino = aulaCapacitacionRepository
                .buscarActivaPorIdConBloqueo(request.aulaCapacitacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un aula de capacitación activa."
                ));
        if (!aulaDestino.getCapacitacion().getId().equals(capacitacionId)) {
            throw new ReglaNegocioException(
                    "El aula de destino no pertenece a esta capacitación."
            );
        }
        AulaClient.AulaResponse aula = aulaClient.buscarPorId(
                aulaDestino.getAulaId(),
                token
        );
        if (aula == null || !Boolean.TRUE.equals(aula.activa())) {
            throw new ReglaNegocioException(
                    "El aula de destino está inactiva."
            );
        }
        if (aula.capacidad() == null || aula.capacidad() <= 0) {
            throw new ReglaNegocioException(
                    "El aula de destino no tiene una capacidad válida."
            );
        }
        if (!funcion.name().equalsIgnoreCase(aula.tipo())) {
            throw new ReglaNegocioException(
                    "La función " + funcion.name()
                            + " no es compatible con el tipo del aula."
            );
        }
        boolean mismaAula = asignacion.getAulaCapacitacion()
                .getId()
                .equals(aulaDestino.getId());
        if (!mismaAula) {
            long ocupacion = asignacionRepository
                    .countByAulaCapacitacion_IdAndActivaTrue(
                            aulaDestino.getId()
                    );

            if (ocupacion >= aula.capacidad()) {
                throw new ReglaNegocioException(
                        "El aula de destino no tiene capacidad disponible."
                );
            }
        }
        asignacion.setAulaCapacitacion(aulaDestino);
        asignacion.setFuncion(funcion.name());
        AsignacionParticipante actualizada = asignacionRepository.save(
                asignacion
        );
        PersonalClient.PersonalResponse personal = personalClient.buscarPorId(
                actualizada.getPersonalId(),
                token
        );
        return mapper.toResponse(actualizada, personal, aula);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AsignacionParticipanteResponse> listarPorCapacitacion(UUID capacitacionId, String token) {
        capacitacionRepository.findById(capacitacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la capacitación."
                ));
        List<AsignacionParticipante> asignaciones =
                asignacionRepository.findAllByCapacitacion_IdAndActivaTrue(
                        capacitacionId
                );
        return asignaciones.stream()
                .map(asignacion -> {
                    PersonalClient.PersonalResponse personal =
                            personalClient.buscarPorId(
                                    asignacion.getPersonalId(),
                                    token
                            );
                    AulaClient.AulaResponse aula = aulaClient.buscarPorId(
                            asignacion.getAulaCapacitacion().getAulaId(),
                            token
                    );
                    return mapper.toResponse(asignacion, personal, aula);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenIngresosResponse obtenerResumenIngresos(
            UUID capacitacionId,
            String token
    ) {
        capacitacionRepository.findById(capacitacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la capacitación."
                ));
        List<AsignacionParticipante> asignaciones = asignacionRepository.findAllByCapacitacion_IdAndActivaTrue(
                capacitacionId
        );
        long titulares = 0;
        long reservas = 0;
        long aplicadores = 0;
        long orientadores = 0;
        for (AsignacionParticipante asignacion : asignaciones) {
            PersonalClient.PersonalResponse personal = personalClient.buscarPorId(
                    asignacion.getPersonalId(),
                    token
            );
            if ("TITULAR".equalsIgnoreCase(personal.situacion())) {
                titulares++;
            } else if ("RESERVA".equalsIgnoreCase(personal.situacion())) {
                reservas++;
            }
            if ("APLICADOR".equalsIgnoreCase(asignacion.getFuncion())) {
                aplicadores++;
            } else if ("ORIENTADOR".equalsIgnoreCase(asignacion.getFuncion())) {
                orientadores++;
            }
        }
        return new ResumenIngresosResponse(
                capacitacionId,
                asignaciones.size(),
                titulares,
                reservas,
                aplicadores,
                orientadores
        );
    }

}
