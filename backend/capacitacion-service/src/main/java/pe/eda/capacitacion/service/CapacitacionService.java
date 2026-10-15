package pe.eda.capacitacion.service;

import pe.eda.capacitacion.dto.request.CapacitacionRequest;
import pe.eda.capacitacion.dto.response.CapacitacionResponse;
import pe.eda.capacitacion.enums.EstadoCapacitacion;

import java.util.List;
import java.util.UUID;

public interface CapacitacionService {
    CapacitacionResponse registrar(CapacitacionRequest request);

    CapacitacionResponse buscarPorId(UUID id);

    List<CapacitacionResponse> listarTodas();

    List<CapacitacionResponse> listarPorAnio(Integer anio);

    List<CapacitacionResponse> listarPorEstado(EstadoCapacitacion estado);

    CapacitacionResponse iniciarCapacitacion(UUID id);
}