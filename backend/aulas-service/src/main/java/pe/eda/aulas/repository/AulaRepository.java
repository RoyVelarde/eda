package pe.eda.aulas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.eda.aulas.entity.Aula;
import pe.eda.aulas.enums.TipoAula;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AulaRepository extends JpaRepository<Aula, UUID> {
    boolean existsByCodigoIgnoreCase(String codigo);

    Optional<Aula> findByCodigoIgnoreCase(String codigo);

    List<Aula> findAllByOrderByCodigoAsc();

    List<Aula> findAllByTipoOrderByCodigoAsc(TipoAula tipo);

    List<Aula> findAllByActivaTrueOrderByCodigoAsc();
}