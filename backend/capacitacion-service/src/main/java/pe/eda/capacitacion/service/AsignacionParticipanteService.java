package pe.eda.capacitacion.service;

import pe.eda.capacitacion.dto.request.AsignacionParticipanteRequest;
import pe.eda.capacitacion.dto.response.AsignacionParticipanteResponse;

import java.util.UUID;

public interface AsignacionParticipanteService {
    AsignacionParticipanteResponse asignar(
            UUID capacitacionId,
            AsignacionParticipanteRequest request,
            String token
    );
}
