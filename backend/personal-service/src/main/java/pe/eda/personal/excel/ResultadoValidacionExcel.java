package pe.eda.personal.excel;

import java.util.List;

public record ResultadoValidacionExcel(
        int totalFilas,
        int filasValidas,
        int filasConError,
        int registrosNuevos,
        int registrosExistentes,
        List<String> errores
) {
}
