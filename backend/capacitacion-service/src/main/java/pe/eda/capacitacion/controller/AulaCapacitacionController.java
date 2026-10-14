package pe.eda.capacitacion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.eda.capacitacion.dto.request.AulaCapacitacionRequest;
import pe.eda.capacitacion.dto.response.AulaCapacitacionResponse;
import pe.eda.capacitacion.service.AulaCapacitacionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/capacitaciones/{capacitacionId}/aulas")
@RequiredArgsConstructor
public class AulaCapacitacionController {
    private final AulaCapacitacionService aulaCapacitacionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AulaCapacitacionResponse asignarAula(
            @PathVariable UUID capacitacionId,
            @Valid @RequestBody AulaCapacitacionRequest request,
            JwtAuthenticationToken authentication
    ) {
        String token = authentication.getToken().getTokenValue();
        return aulaCapacitacionService.asignarAula(capacitacionId, request, token);
    }

    @GetMapping
    public List<AulaCapacitacionResponse> listarAulas(
            @PathVariable UUID capacitacionId,
            JwtAuthenticationToken authentication
    ) {
        String token = authentication.getToken().getTokenValue();
        return aulaCapacitacionService.listarAulas(capacitacionId, token);
    }
}