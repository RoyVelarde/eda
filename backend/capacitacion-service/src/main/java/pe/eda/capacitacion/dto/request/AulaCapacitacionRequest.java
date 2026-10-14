package pe.eda.capacitacion.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AulaCapacitacionRequest(
        @NotNull(message = "El ID del aula es obligatorio")
        UUID aulaId
) {
}