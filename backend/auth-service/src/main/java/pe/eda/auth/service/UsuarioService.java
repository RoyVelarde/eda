package pe.eda.auth.service;

import pe.eda.auth.dto.request.UsuarioRequest;
import pe.eda.auth.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {
    UsuarioResponse crear(UsuarioRequest request);
    List<UsuarioResponse> listar();
}
