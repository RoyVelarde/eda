package pe.eda.capacitacion.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AsignacionParticipanteResponse(
        UUID id,
        UUID capacitacionId,
        UUID personalId,
        String dni,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombres,
        String cargo,
        String funcion,
        String situacion,
        Integer ranking,
        UUID aulaCapacitacionId,
        UUID aulaId,
        String codigoAula,
        String tipoAula,
        LocalDateTime fechaAsignacion
) {}