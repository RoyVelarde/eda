package pe.eda.capacitacion.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CodigoReservaResponse(
        UUID id,
        UUID capacitacionId,
        boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {}
