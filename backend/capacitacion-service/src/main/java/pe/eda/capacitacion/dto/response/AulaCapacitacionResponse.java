package pe.eda.capacitacion.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AulaCapacitacionResponse(
        UUID id,
        UUID capacitacionId,
        UUID aulaId,
        String codigoAula,
        String tipoAula,
        Integer capacidad,
        Boolean activa,
        LocalDateTime fechaAsignacion
) {
}
