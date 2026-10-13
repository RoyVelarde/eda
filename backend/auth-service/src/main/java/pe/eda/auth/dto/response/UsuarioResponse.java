package pe.eda.auth.dto.response;

import pe.eda.auth.enums.RolUsuario;

import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String username,
        String nombres,
        String apellidos,
        RolUsuario rol,
        Boolean activo
) {
}
