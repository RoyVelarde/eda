package pe.eda.capacitacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodigoReservaRequest(
        @NotBlank(message = "El código de reserva es obligatorio")
        @Size(min = 6, max = 100, message = "El código debe tener entre 6 y 100 caracteres")
        String codigo
) {}