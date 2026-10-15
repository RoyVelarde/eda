package pe.eda.capacitacion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import pe.eda.capacitacion.dto.request.ActualizarAsignacionTutorRequest;
import pe.eda.capacitacion.dto.request.AsignacionTutorRequest;
import pe.eda.capacitacion.dto.response.AsignacionTutorResponse;
import pe.eda.capacitacion.service.AsignacionTutorService;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/capacitaciones/{capacitacionId}")
@RequiredArgsConstructor
public class AsignacionTutorController {
    private final AsignacionTutorService asignacionTutorService;

    @PostMapping("/aulas/{aulaCapacitacionId}/tutores")
    public AsignacionTutorResponse asignarTutor(
            @PathVariable UUID capacitacionId,
            @PathVariable UUID aulaCapacitacionId,
            @Valid @RequestBody AsignacionTutorRequest request,
            JwtAuthenticationToken authentication
    ) {
        String token = authentication.getToken().getTokenValue();
        return asignacionTutorService.asignarTutor(
                capacitacionId,
                aulaCapacitacionId,
                request,
                token
        );
    }

    @GetMapping("/tutores")
    public List<AsignacionTutorResponse> listarTutores(
            @PathVariable UUID capacitacionId,
            JwtAuthenticationToken authentication
    ) {
        String token = authentication.getToken().getTokenValue();
        return asignacionTutorService.listarTutores(
                capacitacionId,
                token
        );
    }

    @PutMapping("/tutores/{asignacionTutorId}")
    public AsignacionTutorResponse actualizarAsignacion(
            @PathVariable UUID capacitacionId,
            @PathVariable UUID asignacionTutorId,
            @Valid @RequestBody ActualizarAsignacionTutorRequest request,
            JwtAuthenticationToken authentication
    ) {
        String token = authentication.getToken().getTokenValue();
        return asignacionTutorService.actualizarAsignacion(
                capacitacionId,
                asignacionTutorId,
                request,
                token
        );
    }

    @DeleteMapping("/tutores/{asignacionTutorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarTutor(@PathVariable UUID capacitacionId, @PathVariable UUID asignacionTutorId) {
        asignacionTutorService.quitarTutor(
                capacitacionId,
                asignacionTutorId
        );
    }
}