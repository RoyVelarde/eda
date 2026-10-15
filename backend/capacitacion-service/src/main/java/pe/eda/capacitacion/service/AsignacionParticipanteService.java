package pe.eda.capacitacion.service;

import pe.eda.capacitacion.dto.request.AsignacionParticipanteRequest;
import pe.eda.capacitacion.dto.response.AsignacionParticipanteResponse;
import pe.eda.capacitacion.dto.request.CambiarAsignacionParticipanteRequest;

import java.util.List;
import java.util.UUID;

public interface AsignacionParticipanteService {
    AsignacionParticipanteResponse asignar(
            UUID capacitacionId,
            AsignacionParticipanteRequest request,
            String token
    );

    AsignacionParticipanteResponse cambiarAsignacion(
            UUID capacitacionId,
            UUID asignacionId,
            CambiarAsignacionParticipanteRequest request,
            String token
    );

    List<AsignacionParticipanteResponse> listarPorCapacitacion(
            UUID capacitacionId,
            String token
    );
}
