package pe.eda.personal.excel;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelPersonalReader {

    public List<List<String>> leerFilas(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo Excel está vacío.");
        }
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || !nombre.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("El archivo debe tener formato .xlsx.");
        }
        List<List<String>> filas = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {
            Sheet hoja = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (Row fila : hoja) {
                List<String> celdas = new ArrayList<>();
                for (int i = 0; i < fila.getLastCellNum(); i++) {
                    Cell celda = fila.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String valor = celda == null ? "" : formatter.formatCellValue(celda).trim();
                    celdas.add(valor);
                }
                filas.add(celdas);
            }
        }
        return filas;
    }
}