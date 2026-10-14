package pe.eda.personal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.eda.personal.dto.response.ImportacionResponse;
import pe.eda.personal.enums.TipoImportacion;
import pe.eda.personal.service.PersonalService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/personal/importaciones")
@RequiredArgsConstructor
public class ExcelImportController {
    private final PersonalService personalService;

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportacionResponse validarArchivo(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("nivel") TipoImportacion nivel
    ) throws IOException {
        return personalService.importarExcel(archivo, nivel);
    }
}
