package pe.eda.auth.mapper;

import org.springframework.stereotype.Component;
import pe.eda.auth.dto.response.UsuarioResponse;
import pe.eda.auth.entity.Usuario;

@Component
public class UsuarioMapper {
    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getRol(),
                usuario.getActivo()
        );
    }
}