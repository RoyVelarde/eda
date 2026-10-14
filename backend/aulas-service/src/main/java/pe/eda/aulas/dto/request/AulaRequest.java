package pe.eda.aulas.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.eda.aulas.enums.TipoAula;

public record AulaRequest(
        @NotBlank(message = "El código del aula es obligatorio")
        @Size(max = 20, message = "El código no puede superar los 20 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "El código solo puede contener letras, números, guiones y guiones bajos")
        String codigo,

        @NotNull(message = "El tipo de aula es obligatorio")
        TipoAula tipo,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad mínima es 1 participante")
        @Max(value = 500, message = "La capacidad máxima es 500 participantes")
        Integer capacidad
) {
}
