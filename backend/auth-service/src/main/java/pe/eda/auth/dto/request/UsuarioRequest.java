package pe.eda.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.eda.auth.enums.RolUsuario;

public record UsuarioRequest(
        @NotBlank
        @Size(min = 4, max = 50)
        String username,

        @NotBlank
        @Size(min = 12, max = 72)
        String password,

        @NotBlank
        String nombres,

        @NotBlank
        String apellidos,

        @NotNull
        RolUsuario rol
) {
}
