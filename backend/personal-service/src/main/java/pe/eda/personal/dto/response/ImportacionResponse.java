package pe.eda.personal.dto.response;

import java.util.List;

public record ImportacionResponse(
        String archivo,
        String nivel,
        boolean valido,
        int totalFilas,
        int filasValidas,
        int filasConError,
        int registrosNuevos,
        int registrosExistentes,
        List<String> errores
) {
}
