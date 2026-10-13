package pe.eda.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.eda.auth.dto.request.UsuarioRequest;
import pe.eda.auth.dto.response.UsuarioResponse;
import pe.eda.auth.entity.Usuario;
import pe.eda.auth.exception.RecursoDuplicadoException;
import pe.eda.auth.mapper.UsuarioMapper;
import pe.eda.auth.repository.UsuarioRepository;
import pe.eda.auth.service.UsuarioService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new RecursoDuplicadoException("El nombre de usuario ya existe");
        }
        Usuario usuario = Usuario.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .nombres(request.nombres())
                .apellidos(request.apellidos())
                .rol(request.rol())
                .activo(true)
                .build();
        Usuario guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }
}
