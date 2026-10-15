package pe.eda.capacitacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CambiarAsignacionParticipanteRequest(

        @NotNull(message = "El aula de capacitación es obligatoria")
        UUID aulaCapacitacionId,

        @NotBlank(message = "La función es obligatoria")
        String funcion

) {}
