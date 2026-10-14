package pe.eda.personal.dto.response;

import pe.eda.personal.enums.NivelPersonal;

import java.time.LocalDateTime;
import java.util.UUID;

public record PersonalResponse(
        UUID id,
        String dni,
        NivelPersonal nivel,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombres,
        String celular,
        String correo,
        String cargo,
        String situacion,
        Integer ranking,
        LocalDateTime fechaCreacion
) {
}