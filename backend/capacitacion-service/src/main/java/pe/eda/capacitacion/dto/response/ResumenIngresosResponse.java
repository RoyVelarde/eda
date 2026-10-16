package pe.eda.capacitacion.dto.response;

import java.util.UUID;

public record ResumenIngresosResponse(
        UUID capacitacionId,
        long totalIngresantes,
        long titularesIngresados,
        long reservasIngresados,
        long aplicadoresIngresados,
        long orientadoresIngresados
) {
}