package pe.eda.personal.excel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.eda.personal.enums.TipoImportacion;
import pe.eda.personal.repository.PersonalRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ExcelValidationService {
    private static final Pattern NUMERO_FILA = Pattern.compile("^Fila (\\d+):");
    private final ExcelPersonalReader excelPersonalReader;
    private final ExcelHeaderValidator excelHeaderValidator;
    private final ExcelRowValidator excelRowValidator;
    private final PersonalRepository personalRepository;

    public ResultadoValidacionExcel validarArchivo(MultipartFile archivo, TipoImportacion tipo) throws IOException {
        List<List<String>> filas = excelPersonalReader.leerFilas(archivo);
        if (filas.isEmpty()) {
            return new ResultadoValidacionExcel(
                    0, 0, 0, 0, 0,
                    List.of("El Excel no contiene encabezados.")
            );
        }
        List<String> errores = new ArrayList<>(excelHeaderValidator.validar(filas.getFirst(), tipo));
        int totalFilas = (int) filas.stream()
                .skip(1)
                .filter(fila -> fila.stream().anyMatch(valor -> valor != null && !valor.isBlank()))
                .count();
        if (!errores.isEmpty()) {
            return new ResultadoValidacionExcel(
                    totalFilas,
                    0,
                    0,
                    0,
                    0,
                    errores
            );
        }
        errores.addAll(excelRowValidator.validar(filas, tipo));
        Set<Integer> filasConError = new HashSet<>();
        for (String error : errores) {
            Matcher matcher = NUMERO_FILA.matcher(error);
            if (matcher.find()) {
                filasConError.add(Integer.parseInt(matcher.group(1)));
            }
        }
        int totalConError = filasConError.size();
        int filasValidas = totalFilas - totalConError;
        int registrosNuevos = 0;
        int registrosExistentes = 0;
        if (filasValidas > 0) {
            int indiceDni = tipo == TipoImportacion.NIVEL_2 ? 3 : 4;
            for (int i = 1; i < filas.size(); i++) {
                List<String> fila = filas.get(i);
                if (fila.stream().allMatch(
                        valor -> valor == null || valor.isBlank())) {
                    continue;
                }
                int numeroFila = i + 1;
                if (filasConError.contains(numeroFila)) {
                    continue;
                }
                if (fila.size() <= indiceDni || fila.get(indiceDni) == null) {
                    continue;
                }
                String dni = fila.get(indiceDni).trim();
                if (dni.isBlank()) {
                    continue;
                }
                if (personalRepository.existsByDni(dni)) {
                    registrosExistentes++;
                } else {
                    registrosNuevos++;
                }
            }
        }
        return new ResultadoValidacionExcel(
                totalFilas,
                filasValidas,
                totalConError,
                registrosNuevos,
                registrosExistentes,
                errores
        );
    }
}