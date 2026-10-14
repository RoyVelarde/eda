package pe.eda.capacitacion.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.eda.capacitacion.dto.request.CapacitacionRequest;
import pe.eda.capacitacion.dto.response.CapacitacionResponse;
import pe.eda.capacitacion.enums.EstadoCapacitacion;
import pe.eda.capacitacion.service.CapacitacionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/capacitaciones")
@Validated
public class CapacitacionController {

    private final CapacitacionService service;

    public CapacitacionController(CapacitacionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CapacitacionResponse registrar(
            @Valid @RequestBody CapacitacionRequest request
    ) {
        return service.registrar(request);
    }

    @GetMapping
    public List<CapacitacionResponse> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public CapacitacionResponse buscarPorId(
            @PathVariable UUID id
    ) {
        return service.buscarPorId(id);
    }

    @GetMapping("/anio/{anio}")
    public List<CapacitacionResponse> listarPorAnio(
            @PathVariable
            @Min(value = 2000, message = "El año mínimo es 2000")
            @Max(value = 2100, message = "El año máximo es 2100")
            Integer anio
    ) {
        return service.listarPorAnio(anio);
    }

    @GetMapping("/estado/{estado}")
    public List<CapacitacionResponse> listarPorEstado(
            @PathVariable EstadoCapacitacion estado
    ) {
        return service.listarPorEstado(estado);
    }
}