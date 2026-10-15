package pe.eda.capacitacion.repository;

import pe.eda.capacitacion.entity.CodigoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CodigoReservaRepository extends JpaRepository<CodigoReserva, UUID> {
    Optional<CodigoReserva> findByCapacitacionId(UUID capacitacionId);

    boolean existsByCapacitacionId(UUID capacitacionId);
}
