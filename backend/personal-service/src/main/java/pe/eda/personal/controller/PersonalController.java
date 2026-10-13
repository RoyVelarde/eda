package pe.eda.personal.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import pe.eda.personal.dto.request.PersonalRequest;
import pe.eda.personal.dto.response.PersonalResponse;
import pe.eda.personal.enums.NivelPersonal;
import pe.eda.personal.service.PersonalService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/personal")
public class PersonalController {

    private final PersonalService personalService;

    public PersonalController(PersonalService personalService) {
        this.personalService = personalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonalResponse registrar(
            @Valid @RequestBody PersonalRequest request
    ) {
        return personalService.registrar(request);
    }

    @GetMapping
    public List<PersonalResponse> listarTodos() {
        return personalService.listarTodos();
    }

    @GetMapping("/{id}")
    public PersonalResponse buscarPorId(@PathVariable UUID id) {
        return personalService.buscarPorId(id);
    }

    @GetMapping("/dni/{dni}")
    public PersonalResponse buscarPorDni(@PathVariable String dni) {
        return personalService.buscarPorDni(dni);
    }

    @GetMapping("/nivel/{nivel}")
    public List<PersonalResponse> listarPorNivel(
            @PathVariable NivelPersonal nivel
    ) {
        return personalService.listarPorNivel(nivel);
    }
}