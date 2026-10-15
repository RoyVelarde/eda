package pe.eda.capacitacion.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ac FROM AulaCapacitacion ac WHERE ac.id = :id AND ac.activa = true")
    Optional<AulaCapacitacion> buscarActivaPorIdConBloqueo(@Param("id") UUID id);
}