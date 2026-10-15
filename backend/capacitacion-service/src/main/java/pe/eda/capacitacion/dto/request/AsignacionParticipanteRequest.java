package pe.eda.capacitacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AsignacionParticipanteRequest(

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(
                regexp = "\\d{8}",
                message = "El DNI debe contener exactamente 8 dígitos"
        )
        String dni,

        String codigoReserva

) {
}