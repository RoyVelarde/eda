package pe.eda.capacitacion.dto.request;

import jakarta.validation.constraints.NotNull;
import pe.eda.capacitacion.enums.FuncionTutor;

import java.util.UUID;

public record ActualizarAsignacionTutorRequest(
        @NotNull(message = "El ID del aula de capacitación es obligatorio")
        UUID aulaCapacitacionId,

        @NotNull(message = "La función del tutor es obligatoria")
        FuncionTutor funcion
) {}
