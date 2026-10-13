package pe.eda.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import pe.eda.auth.dto.request.LoginRequest;
import pe.eda.auth.dto.response.AuthResponse;
import pe.eda.auth.entity.Usuario;
import pe.eda.auth.mapper.UsuarioMapper;
import pe.eda.auth.repository.UsuarioRepository;
import pe.eda.auth.security.JwtService;
import pe.eda.auth.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        Usuario usuario = usuarioRepository.findByUsername(request.username()).orElseThrow();
        String token = jwtService.generateToken(usuario);
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                usuarioMapper.toResponse(usuario)
        );
    }
}
