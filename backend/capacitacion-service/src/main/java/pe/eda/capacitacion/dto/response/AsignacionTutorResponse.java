package pe.eda.capacitacion.dto.response;

import pe.eda.capacitacion.enums.FuncionTutor;

import java.time.LocalDateTime;
import java.util.UUID;

public record AsignacionTutorResponse(
        UUID id,
        UUID capacitacionId,
        UUID aulaCapacitacionId,
        UUID personalId,
        String dni,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombres,
        String cargo,
        FuncionTutor funcion,
        Boolean activa,
        LocalDateTime fechaAsignacion
) {
}
