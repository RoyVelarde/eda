package pe.eda.capacitacion.service.impl;

import lombok.RequiredArgsConstructor;
import pe.eda.capacitacion.client.AulaClient;
import pe.eda.capacitacion.dto.request.AulaCapacitacionRequest;
import pe.eda.capacitacion.dto.response.AulaCapacitacionResponse;
import pe.eda.capacitacion.entity.AulaCapacitacion;
import pe.eda.capacitacion.entity.Capacitacion;
import pe.eda.capacitacion.exception.AulaServicioException;
import pe.eda.capacitacion.exception.RecursoDuplicadoException;
import pe.eda.capacitacion.exception.RecursoNoEncontradoException;
import pe.eda.capacitacion.mapper.AulaCapacitacionMapper;
import pe.eda.capacitacion.repository.AulaCapacitacionRepository;
import pe.eda.capacitacion.repository.CapacitacionRepository;
import pe.eda.capacitacion.service.AulaCapacitacionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AulaCapacitacionServiceImpl implements AulaCapacitacionService {
    private final AulaCapacitacionRepository aulaCapacitacionRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final AulaClient aulaClient;
    private final AulaCapacitacionMapper aulaCapacitacionMapper;

    @Override
    @Transactional
    public AulaCapacitacionResponse asignarAula(
            UUID capacitacionId,
            AulaCapacitacionRequest request,
            String token
    ) {
        Capacitacion capacitacion = capacitacionRepository.findById(capacitacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la capacitación con ID: " + capacitacionId
                ));
        AulaClient.AulaResponse aula = aulaClient.buscarPorId(
                request.aulaId(),
                token
        );
        if (aula == null || aula.id() == null) {
            throw new AulaServicioException(
                    "El servicio de aulas devolvió una respuesta inválida."
            );
        }
        if (!Boolean.TRUE.equals(aula.activa())) {
            throw new IllegalStateException(
                    "No se puede asignar un aula inactiva."
            );
        }
        if (aulaCapacitacionRepository.existsByCapacitacion_IdAndAulaId(
                capacitacionId,
                aula.id()
        )) {
            throw new RecursoDuplicadoException(
                    "El aula ya está registrada en esta capacitación."
            );
        }
        AulaCapacitacion asignacion = new AulaCapacitacion();
        asignacion.setCapacitacion(capacitacion);
        asignacion.setAulaId(aula.id());
        AulaCapacitacion guardada = aulaCapacitacionRepository.save(asignacion);
        return aulaCapacitacionMapper.toResponse(guardada, aula);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AulaCapacitacionResponse> listarAulas(
            UUID capacitacionId,
            String token
    ) {
        if (!capacitacionRepository.existsById(capacitacionId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró la capacitación con ID: " + capacitacionId
            );
        }
        return aulaCapacitacionRepository
                .findAllByCapacitacion_IdAndActivaTrueOrderByFechaAsignacionAsc(
                        capacitacionId
                )
                .stream()
                .map(asignacion -> {
                    AulaClient.AulaResponse aula = aulaClient.buscarPorId(
                            asignacion.getAulaId(),
                            token
                    );
                    return aulaCapacitacionMapper.toResponse(asignacion, aula);
                })
                .toList();
    }
}