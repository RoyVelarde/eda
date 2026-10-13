package pe.eda.auth.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.eda.auth.entity.Usuario;
import pe.eda.auth.enums.RolUsuario;
import pe.eda.auth.repository.UsuarioRepository;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.enabled:false}")
    private boolean enabled;

    @Value("${app.bootstrap.username:}")
    private String username;

    @Value("${app.bootstrap.password:}")
    private String password;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Faltan credenciales para crear el CTS inicial");
        }
        if (usuarioRepository.existsByUsername(username)) {
            return;
        }
        Usuario usuario = Usuario.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nombres("Administrador")
                .apellidos("CTS")
                .rol(RolUsuario.CTS)
                .activo(true)
                .build();
        usuarioRepository.save(usuario);
    }
}
