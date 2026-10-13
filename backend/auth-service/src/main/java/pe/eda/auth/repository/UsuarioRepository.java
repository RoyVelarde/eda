package pe.eda.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.eda.auth.entity.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}