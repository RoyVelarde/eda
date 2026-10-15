package pe.eda.capacitacion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.eda.capacitacion.dto.request.AsignacionParticipanteRequest;
import pe.eda.capacitacion.dto.response.AsignacionParticipanteResponse;
import pe.eda.capacitacion.service.AsignacionParticipanteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/capacitaciones/{capacitacionId}/participantes")
@RequiredArgsConstructor
public class AsignacionParticipanteController {
    private final AsignacionParticipanteService asignacionParticipanteService;

    @PostMapping("/asignaciones")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CTS', 'CAS')")
    public AsignacionParticipanteResponse asignar(
            @PathVariable UUID capacitacionId,
            @Valid @RequestBody AsignacionParticipanteRequest request,
            JwtAuthenticationToken authentication
    ) {
        String token = authentication.getToken().getTokenValue();
        return asignacionParticipanteService.asignar(capacitacionId, request, token);
    }
}
