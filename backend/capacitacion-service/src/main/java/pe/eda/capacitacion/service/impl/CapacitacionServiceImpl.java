package pe.eda.capacitacion.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.eda.capacitacion.dto.request.CapacitacionRequest;
import pe.eda.capacitacion.dto.response.CapacitacionResponse;
import pe.eda.capacitacion.entity.Capacitacion;
import pe.eda.capacitacion.enums.EstadoCapacitacion;
import pe.eda.capacitacion.exception.RecursoNoEncontradoException;
import pe.eda.capacitacion.exception.ReglaNegocioException;
import pe.eda.capacitacion.mapper.CapacitacionMapper;
import pe.eda.capacitacion.repository.CapacitacionRepository;
import pe.eda.capacitacion.service.CapacitacionService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CapacitacionServiceImpl implements CapacitacionService {
    private final CapacitacionRepository repository;
    private final CapacitacionMapper mapper;

    @Override
    @Transactional
    public CapacitacionResponse registrar(CapacitacionRequest request) {
        validarFechas(request);
        Capacitacion capacitacion = mapper.toEntity(request);
        capacitacion.setEstado(EstadoCapacitacion.PLANIFICADA);
        Capacitacion guardada = repository.save(capacitacion);
        return mapper.toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CapacitacionResponse buscarPorId(UUID id) {
        Capacitacion capacitacion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una capacitación con ID: " + id
                ));
        return mapper.toResponse(capacitacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CapacitacionResponse> listarTodas() {
        return repository.findAllByOrderByFechaInicioDesc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CapacitacionResponse> listarPorAnio(Integer anio) {
        return repository.findAllByAnioOrderByFechaInicioDesc(anio)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CapacitacionResponse> listarPorEstado(
            EstadoCapacitacion estado
    ) {
        return repository.findAllByEstadoOrderByFechaInicioDesc(estado)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void validarFechas(CapacitacionRequest request) {
        if (request.fechaInicio() == null ||
                request.fechaFin() == null ||
                request.anio() == null) {
            throw new ReglaNegocioException(
                    "El año y las fechas de capacitación son obligatorios"
            );
        }
        if (request.fechaFin().isBefore(request.fechaInicio())) {
            throw new ReglaNegocioException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio"
            );
        }
        if (request.anio() != request.fechaInicio().getYear()) {
            throw new ReglaNegocioException(
                    "El año de la capacitación debe coincidir con el año de inicio"
            );
        }
    }
}
