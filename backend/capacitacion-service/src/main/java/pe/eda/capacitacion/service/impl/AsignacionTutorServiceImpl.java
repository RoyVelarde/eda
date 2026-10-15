package pe.eda.capacitacion.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.eda.capacitacion.client.PersonalClient;
import pe.eda.capacitacion.dto.request.ActualizarAsignacionTutorRequest;
import pe.eda.capacitacion.dto.request.AsignacionTutorRequest;
import pe.eda.capacitacion.dto.response.AsignacionTutorResponse;
import pe.eda.capacitacion.entity.AsignacionTutor;
import pe.eda.capacitacion.entity.AulaCapacitacion;
import pe.eda.capacitacion.entity.Capacitacion;
import pe.eda.capacitacion.enums.EstadoCapacitacion;
import pe.eda.capacitacion.enums.FuncionTutor;
import pe.eda.capacitacion.exception.RecursoDuplicadoException;
import pe.eda.capacitacion.exception.RecursoNoEncontradoException;
import pe.eda.capacitacion.exception.ReglaNegocioException;
import pe.eda.capacitacion.mapper.AsignacionTutorMapper;
import pe.eda.capacitacion.repository.AsignacionTutorRepository;
import pe.eda.capacitacion.repository.AulaCapacitacionRepository;
import pe.eda.capacitacion.repository.CapacitacionRepository;
import pe.eda.capacitacion.service.AsignacionTutorService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AsignacionTutorServiceImpl implements AsignacionTutorService {
    private final AsignacionTutorRepository asignacionTutorRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final AulaCapacitacionRepository aulaCapacitacionRepository;
    private final PersonalClient personalClient;
    private final AsignacionTutorMapper asignacionTutorMapper;

    @Override
    @Transactional
    public AsignacionTutorResponse asignarTutor(
            UUID capacitacionId,
            UUID aulaCapacitacionId,
            AsignacionTutorRequest request,
            String token
    ) {
        Capacitacion capacitacion = capacitacionRepository.findById(capacitacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la capacitación con ID: " + capacitacionId
                ));
        if (capacitacion.getEstado() == EstadoCapacitacion.FINALIZADA
                || capacitacion.getEstado() == EstadoCapacitacion.CANCELADA) {
            throw new ReglaNegocioException(
                    "No se pueden asignar tutores a una capacitación finalizada o cancelada."
            );
        }
        AulaCapacitacion aulaCapacitacion =
                aulaCapacitacionRepository.findById(aulaCapacitacionId)
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "No se encontró la asignación de aula con ID: "
                                        + aulaCapacitacionId
                        ));
        if (!aulaCapacitacion.getCapacitacion().getId().equals(capacitacionId)) {
            throw new ReglaNegocioException(
                    "El aula no pertenece a la capacitación indicada."
            );
        }
        if (!Boolean.TRUE.equals(aulaCapacitacion.getActiva())) {
            throw new ReglaNegocioException(
                    "No se pueden asignar tutores a un aula inactiva."
            );
        }
        PersonalClient.PersonalResponse personal =
                personalClient.buscarPorId(request.personalId(), token);
        if (personal == null || personal.id() == null) {
            throw new ReglaNegocioException(
                    "No fue posible validar los datos del tutor."
            );
        }
        if (!"NIVEL_2".equals(personal.nivel())) {
            throw new ReglaNegocioException(
                    "Solo se pueden asignar personas de Nivel 2 como tutores."
            );
        }
        if (asignacionTutorRepository
                .existsByCapacitacion_IdAndPersonalIdAndActivaTrue(
                        capacitacionId,
                        request.personalId()
                )) {
            throw new RecursoDuplicadoException(
                    "El tutor ya está asignado a un aula de esta capacitación."
            );
        }
        long cantidadTutores = asignacionTutorRepository
                .countByAulaCapacitacion_IdAndActivaTrue(aulaCapacitacionId);
        if (cantidadTutores >= 2) {
            throw new ReglaNegocioException(
                    "El aula ya tiene el máximo de dos tutores activos."
            );
        }
        boolean funcionOcupada = asignacionTutorRepository
                .findByAulaCapacitacion_IdAndFuncionAndActivaTrue(
                        aulaCapacitacionId,
                        request.funcion()
                )
                .isPresent();
        if (funcionOcupada) {
            throw new RecursoDuplicadoException(
                    "El aula ya tiene un tutor asignado con la función "
                            + request.funcion() + "."
            );
        }
        AsignacionTutor asignacion = new AsignacionTutor();
        asignacion.setCapacitacion(capacitacion);
        asignacion.setAulaCapacitacion(aulaCapacitacion);
        asignacion.setPersonalId(request.personalId());
        asignacion.setFuncion(request.funcion());
        asignacion.setActiva(true);
        AsignacionTutor guardada = asignacionTutorRepository.save(asignacion);
        return asignacionTutorMapper.toResponse(guardada, personal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionTutorResponse> listarTutores(
            UUID capacitacionId,
            String token
    ) {
        if (!capacitacionRepository.existsById(capacitacionId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró la capacitación con ID: " + capacitacionId
            );
        }
        return asignacionTutorRepository
                .findAllByCapacitacion_IdAndActivaTrue(capacitacionId)
                .stream()
                .map(asignacion -> {
                    PersonalClient.PersonalResponse personal =
                            personalClient.buscarPorId(
                                    asignacion.getPersonalId(),
                                    token
                            );
                    return asignacionTutorMapper.toResponse(asignacion, personal);
                })
                .toList();
    }

    @Override
    @Transactional
    public AsignacionTutorResponse actualizarAsignacion(
            UUID capacitacionId,
            UUID asignacionTutorId,
            ActualizarAsignacionTutorRequest request,
            String token
    ) {
        Capacitacion capacitacion = capacitacionRepository.findById(capacitacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la capacitación."));
        if (capacitacion.getEstado() == EstadoCapacitacion.FINALIZADA
                || capacitacion.getEstado() == EstadoCapacitacion.CANCELADA) {
            throw new ReglaNegocioException(
                    "No se pueden modificar tutores de una capacitación finalizada o cancelada.");
        }
        AsignacionTutor asignacion = asignacionTutorRepository
                .findById(asignacionTutorId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la asignación del tutor."));
        if (!asignacion.getCapacitacion().getId().equals(capacitacionId)) {
            throw new ReglaNegocioException(
                    "La asignación del tutor no pertenece a esta capacitación.");
        }
        if (!Boolean.TRUE.equals(asignacion.getActiva())) {
            throw new ReglaNegocioException(
                    "No se puede modificar una asignación inactiva.");
        }
        UUID aulaDestinoId = request.aulaCapacitacionId();
        AulaCapacitacion aulaDestino = aulaCapacitacionRepository
                .findById(aulaDestinoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el aula asignada a la capacitación."));
        if (!aulaDestino.getCapacitacion().getId().equals(capacitacionId)) {
            throw new ReglaNegocioException(
                    "El aula de destino no pertenece a esta capacitación.");
        }
        if (!Boolean.TRUE.equals(aulaDestino.getActiva())) {
            throw new ReglaNegocioException(
                    "No se puede asignar el tutor a un aula inactiva.");
        }
        boolean cambiaDeAula = !asignacion.getAulaCapacitacion()
                .getId().equals(aulaDestinoId);
        if (cambiaDeAula) {
            long tutoresDestino = asignacionTutorRepository
                    .countByAulaCapacitacion_IdAndActivaTrue(aulaDestinoId);
            if (tutoresDestino >= 2) {
                throw new RecursoDuplicadoException(
                        "El aula de destino ya tiene el máximo de dos tutores.");
            }
        }
        if (request.funcion() == FuncionTutor.RESPONSABLE) {
            Optional<AsignacionTutor> responsableExistente =
                    asignacionTutorRepository
                            .findByAulaCapacitacion_IdAndFuncionAndActivaTrue(
                                    aulaDestinoId, FuncionTutor.RESPONSABLE);
            if (responsableExistente.isPresent()
                    && !responsableExistente.get().getId().equals(asignacionTutorId)) {
                throw new RecursoDuplicadoException(
                        "El aula de destino ya tiene un tutor responsable.");
            }
        }
        asignacion.setAulaCapacitacion(aulaDestino);
        asignacion.setFuncion(request.funcion());
        AsignacionTutor actualizada = asignacionTutorRepository.save(asignacion);
        PersonalClient.PersonalResponse personal =
                personalClient.buscarPorId(actualizada.getPersonalId(), token);
        return asignacionTutorMapper.toResponse(actualizada, personal);
    }

    @Override
    @Transactional
    public void quitarTutor(
            UUID capacitacionId,
            UUID asignacionTutorId) {
        Capacitacion capacitacion = capacitacionRepository.findById(capacitacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la capacitación."));
        if (capacitacion.getEstado() == EstadoCapacitacion.FINALIZADA
                || capacitacion.getEstado() == EstadoCapacitacion.CANCELADA) {
            throw new ReglaNegocioException(
                    "No se pueden quitar tutores de una capacitación finalizada o cancelada.");
        }
        AsignacionTutor asignacion = asignacionTutorRepository
                .findById(asignacionTutorId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la asignación del tutor."));
        if (!asignacion.getCapacitacion().getId().equals(capacitacionId)) {
            throw new ReglaNegocioException(
                    "La asignación del tutor no pertenece a esta capacitación.");
        }
        if (!Boolean.TRUE.equals(asignacion.getActiva())) {
            throw new ReglaNegocioException(
                    "El tutor ya fue quitado de esta capacitación.");
        }
        asignacion.setActiva(false);
        asignacionTutorRepository.save(asignacion);
    }
}
