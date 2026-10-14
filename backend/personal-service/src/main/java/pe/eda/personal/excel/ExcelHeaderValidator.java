package pe.eda.personal.excel;

import org.springframework.stereotype.Component;
import pe.eda.personal.enums.TipoImportacion;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ExcelHeaderValidator {

    public List<String> validar(List<String> encabezados, TipoImportacion tipo) {
        List<String> esperados = switch (tipo) {
            case NIVEL_2 -> List.of(
                    "apellido paterno",
                    "apellido materno",
                    "nombres",
                    "dni",
                    "celular",
                    "correo",
                    "cargo"
            );
            case NIVEL_3 -> List.of(
                    "ranking",
                    "apellido paterno",
                    "apellido materno",
                    "nombres",
                    "dni",
                    "correo",
                    "celular",
                    "cargo",
                    "situacion"
            );
        };
        List<String> normalizados = encabezados.stream()
                .map(this::normalizar)
                .toList();
        List<String> errores = new ArrayList<>();
        for (int i = 0; i < esperados.size(); i++) {
            String esperado = normalizar(esperados.get(i));
            if (!normalizados.contains(esperado)) {
                errores.add("Falta la columna obligatoria: " + esperados.get(i));
            } else if (i >= normalizados.size() || !normalizados.get(i).equals(esperado)) {
                errores.add("La columna '" + esperados.get(i) + "' debe estar en la posición " + (i + 1) + ".");
            }
        }
        return errores;
    }

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
