package pe.eda.capacitacion.controller;

import lombok.RequiredArgsConstructor;
import pe.eda.capacitacion.dto.request.CodigoReservaRequest;
import pe.eda.capacitacion.dto.response.CodigoReservaResponse;
import pe.eda.capacitacion.service.CodigoReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/capacitaciones/{capacitacionId}/codigo-reserva")
@RequiredArgsConstructor
public class CodigoReservaController {
    private final CodigoReservaService codigoReservaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CTS')")
    public CodigoReservaResponse configurar(
            @PathVariable UUID capacitacionId,
            @Valid @RequestBody CodigoReservaRequest request
    ) {
        return codigoReservaService.configurar(capacitacionId, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CTS', 'CS', 'CAS')")
    public CodigoReservaResponse consultar(@PathVariable UUID capacitacionId) {
        return codigoReservaService.consultar(capacitacionId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CTS')")
    public void desactivar(@PathVariable UUID capacitacionId) {
        codigoReservaService.desactivar(capacitacionId);
    }
}