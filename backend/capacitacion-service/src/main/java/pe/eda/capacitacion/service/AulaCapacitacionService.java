package pe.eda.capacitacion.service;

import pe.eda.capacitacion.dto.request.AulaCapacitacionRequest;
import pe.eda.capacitacion.dto.response.AulaCapacitacionResponse;

import java.util.List;
import java.util.UUID;

public interface AulaCapacitacionService {

    AulaCapacitacionResponse asignarAula(
            UUID capacitacionId,
            AulaCapacitacionRequest request,
            String token
    );

    List<AulaCapacitacionResponse> listarAulas(UUID capacitacionId, String token);
}
