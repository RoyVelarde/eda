package pe.eda.capacitacion.repository;

import pe.eda.capacitacion.entity.AsignacionParticipante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AsignacionParticipanteRepository extends JpaRepository<AsignacionParticipante, UUID> {

    List<AsignacionParticipante> findAllByCapacitacion_IdAndActivaTrue(
            UUID capacitacionId
    );

    List<AsignacionParticipante> findAllByAulaCapacitacion_IdAndActivaTrue(
            UUID aulaCapacitacionId
    );

    Optional<AsignacionParticipante>
    findByCapacitacion_IdAndPersonalIdAndActivaTrue(
            UUID capacitacionId,
            UUID personalId
    );

    boolean existsByCapacitacion_IdAndPersonalIdAndActivaTrue(
            UUID capacitacionId,
            UUID personalId
    );

    long countByAulaCapacitacion_IdAndActivaTrue(
            UUID aulaCapacitacionId
    );
}