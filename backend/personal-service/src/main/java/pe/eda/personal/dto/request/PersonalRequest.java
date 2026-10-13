package pe.eda.personal.dto.request;

import jakarta.validation.constraints.*;

import pe.eda.personal.enums.NivelPersonal;

public record PersonalRequest(

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe contener exactamente 8 dígitos")
        String dni,

        @NotNull(message = "El nivel es obligatorio")
        NivelPersonal nivel,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(max = 100)
        String apellidoPaterno,

        @NotBlank(message = "El apellido materno es obligatorio")
        @Size(max = 100)
        String apellidoMaterno,

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 150)
        String nombres,

        @Pattern(
                regexp = "\\d{7,15}",
                message = "El celular debe contener entre 7 y 15 dígitos"
        )
        String celular,

        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 150)
        String correo,

        @NotBlank(message = "El cargo es obligatorio")
        @Size(max = 100)
        String cargo,

        @Size(max = 50)
        String situacion
) {
}
