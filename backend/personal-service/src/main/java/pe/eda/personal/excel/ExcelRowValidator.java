package pe.eda.personal.excel;

import org.springframework.stereotype.Component;
import pe.eda.personal.enums.TipoImportacion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class ExcelRowValidator {
    private static final Pattern DNI_PATTERN = Pattern.compile("\\d{8}");
    private static final Pattern CELULAR_PATTERN = Pattern.compile("\\d{7,15}");
    private static final Pattern CORREO_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public List<String> validar(List<List<String>> filas, TipoImportacion tipo) {
        List<String> errores = new ArrayList<>();
        Set<String> dnis = new HashSet<>();
        for (int i = 1; i < filas.size(); i++) {
            List<String> fila = filas.get(i);
            int numeroFila = i + 1;
            if (fila.stream().allMatch(this::vacio)) {
                continue;
            }
            int columnaDni = tipo == TipoImportacion.NIVEL_2 ? 3 : 4;
            int columnaCelular = tipo == TipoImportacion.NIVEL_2 ? 4 : 6;
            int columnaCorreo = 5;
            int columnaCargo = tipo == TipoImportacion.NIVEL_2 ? 6 : 7;
            String apellidoPaterno = obtener(fila, tipo == TipoImportacion.NIVEL_2 ? 0 : 1);
            String apellidoMaterno = obtener(fila, tipo == TipoImportacion.NIVEL_2 ? 1 : 2);
            String nombres = obtener(fila, tipo == TipoImportacion.NIVEL_2 ? 2 : 3);
            String dni = obtener(fila, columnaDni);
            String celular = obtener(fila, columnaCelular);
            String correo = obtener(fila, columnaCorreo);
            String cargo = obtener(fila, columnaCargo);
            if (apellidoPaterno.isBlank()) {
                errores.add("Fila " + numeroFila + ": falta el apellido paterno.");
            }
            if (apellidoMaterno.isBlank()) {
                errores.add("Fila " + numeroFila + ": falta el apellido materno.");
            }
            if (nombres.isBlank()) {
                errores.add("Fila " + numeroFila + ": faltan los nombres.");
            }
            if (cargo.isBlank()) {
                errores.add("Fila " + numeroFila + ": falta el cargo.");
            }
            if (!DNI_PATTERN.matcher(dni).matches()) {
                errores.add("Fila " + numeroFila + ": el DNI debe tener exactamente 8 dígitos.");
            } else if (!dnis.add(dni)) {
                errores.add("Fila " + numeroFila + ": el DNI " + dni + " está repetido en el archivo.");
            }
            if (!celular.isBlank()
                    && !CELULAR_PATTERN.matcher(celular).matches()) {
                errores.add("Fila " + numeroFila + ": el celular debe tener entre 7 y 15 dígitos.");
            }
            if (!correo.isBlank()
                    && !CORREO_PATTERN.matcher(correo).matches()) {
                errores.add("Fila " + numeroFila + ": el correo no tiene un formato válido.");
            }
            if (tipo == TipoImportacion.NIVEL_3) {
                validarRanking(fila, numeroFila, errores);
            }
        }
        return errores;
    }

    private void validarRanking(
            List<String> fila,
            int numeroFila,
            List<String> errores
    ) {
        String valor = obtener(fila, 0);
        try {
            int ranking = Integer.parseInt(valor);

            if (ranking <= 0) {
                errores.add("Fila " + numeroFila + ": el ranking debe ser un entero positivo.");
            }
        } catch (NumberFormatException ex) {
            errores.add("Fila " + numeroFila + ": el ranking debe ser un entero positivo.");
        }
    }

    private String obtener(List<String> fila, int indice) {
        if (indice < 0 || indice >= fila.size()) {
            return "";
        }
        String valor = fila.get(indice);
        return valor == null ? "" : valor.trim();
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
