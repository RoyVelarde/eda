package pe.eda.capacitacion.service;

import pe.eda.capacitacion.dto.request.ActualizarAsignacionTutorRequest;
import pe.eda.capacitacion.dto.request.AsignacionTutorRequest;
import pe.eda.capacitacion.dto.response.AsignacionTutorResponse;

import java.util.List;
import java.util.UUID;

public interface AsignacionTutorService {
    AsignacionTutorResponse asignarTutor(
            UUID capacitacionId,
            UUID aulaCapacitacionId,
            AsignacionTutorRequest request,
            String token
    );

    List<AsignacionTutorResponse> listarTutores(
            UUID capacitacionId,
            String token
    );

    AsignacionTutorResponse actualizarAsignacion(
            UUID capacitacionId,
            UUID asignacionTutorId,
            ActualizarAsignacionTutorRequest request,
            String token
    );

    void quitarTutor(
            UUID capacitacionId,
            UUID asignacionTutorId
    );
}
