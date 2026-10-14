package pe.eda.capacitacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.eda.capacitacion.entity.AulaCapacitacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AulaCapacitacionRepository extends JpaRepository<AulaCapacitacion, UUID> {

    List<AulaCapacitacion> findAllByCapacitacion_IdOrderByFechaAsignacionAsc(
            UUID capacitacionId
    );

    List<AulaCapacitacion>
    findAllByCapacitacion_IdAndActivaTrueOrderByFechaAsignacionAsc(
            UUID capacitacionId
    );

    Optional<AulaCapacitacion> findByCapacitacion_IdAndAulaId(
            UUID capacitacionId,
            UUID aulaId
    );

    boolean existsByCapacitacion_IdAndAulaId(
            UUID capacitacionId,
            UUID aulaId
    );
}