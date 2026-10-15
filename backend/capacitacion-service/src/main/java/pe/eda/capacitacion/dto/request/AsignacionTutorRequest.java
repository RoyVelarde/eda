package pe.eda.capacitacion.dto.request;

import jakarta.validation.constraints.NotNull;
import pe.eda.capacitacion.enums.FuncionTutor;

import java.util.UUID;

public record AsignacionTutorRequest(
        @NotNull(message = "El ID del personal es obligatorio")
        UUID personalId,

        @NotNull(message = "La función del tutor es obligatoria")
        FuncionTutor funcion
) {
}