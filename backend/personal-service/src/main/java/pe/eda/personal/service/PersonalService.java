package pe.eda.personal.service;

import pe.eda.personal.dto.request.PersonalRequest;
import pe.eda.personal.dto.response.PersonalResponse;
import pe.eda.personal.enums.NivelPersonal;

import java.util.List;
import java.util.UUID;

public interface PersonalService {

    PersonalResponse registrar(PersonalRequest request);

    PersonalResponse buscarPorId(UUID id);

    PersonalResponse buscarPorDni(String dni);

    List<PersonalResponse> listarTodos();

    List<PersonalResponse> listarPorNivel(NivelPersonal nivel);
}
