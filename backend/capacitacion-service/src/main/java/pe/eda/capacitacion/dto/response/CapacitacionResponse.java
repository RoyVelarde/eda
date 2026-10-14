package pe.eda.capacitacion.dto.response;

import pe.eda.capacitacion.enums.EstadoCapacitacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CapacitacionResponse(
        UUID id,
        String nombre,
        Integer anio,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        EstadoCapacitacion estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}