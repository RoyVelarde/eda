package pe.eda.aulas.dto.response;

import pe.eda.aulas.enums.TipoAula;

import java.time.LocalDateTime;
import java.util.UUID;

public record AulaResponse(
        UUID id,
        String codigo,
        TipoAula tipo,
        Integer capacidad,
        Boolean activa,
        LocalDateTime fechaCreacion
) {
}
