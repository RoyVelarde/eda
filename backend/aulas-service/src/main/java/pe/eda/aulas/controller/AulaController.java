package pe.eda.aulas.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.eda.aulas.dto.request.AulaRequest;
import pe.eda.aulas.dto.response.AulaResponse;
import pe.eda.aulas.enums.TipoAula;
import pe.eda.aulas.service.AulaService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/aulas")
@RequiredArgsConstructor
public class AulaController {
    private final AulaService aulaService;

    @PostMapping
    public ResponseEntity<AulaResponse> registrar(@Valid @RequestBody AulaRequest request) {
        AulaResponse respuesta = aulaService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<AulaResponse>> listarTodas() {
        return ResponseEntity.ok(aulaService.listarTodas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<AulaResponse>> listarActivas() {
        return ResponseEntity.ok(aulaService.listarActivas());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<AulaResponse>> listarPorTipo(@PathVariable TipoAula tipo) {
        return ResponseEntity.ok(aulaService.listarPorTipo(tipo));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<AulaResponse> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(aulaService.buscarPorCodigo(codigo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AulaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(aulaService.buscarPorId(id));
    }
}
