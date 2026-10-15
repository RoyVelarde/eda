package pe.eda.capacitacion.repository;

import pe.eda.capacitacion.entity.AsignacionTutor;
import pe.eda.capacitacion.enums.FuncionTutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AsignacionTutorRepository extends JpaRepository<AsignacionTutor, UUID> {

    List<AsignacionTutor>
    findAllByCapacitacion_IdAndActivaTrue(UUID capacitacionId);

    List<AsignacionTutor>
    findAllByAulaCapacitacion_IdAndActivaTrue(UUID aulaCapacitacionId);

    Optional<AsignacionTutor>
    findByAulaCapacitacion_IdAndFuncionAndActivaTrue(
            UUID aulaCapacitacionId,
            FuncionTutor funcion
    );

    boolean existsByCapacitacion_IdAndPersonalIdAndActivaTrue(
            UUID capacitacionId,
            UUID personalId
    );

    long countByAulaCapacitacion_IdAndActivaTrue(
            UUID aulaCapacitacionId
    );

    boolean existsByAulaCapacitacion_IdAndPersonalIdAndActivaTrue(
            UUID aulaCapacitacionId,
            UUID personalId
    );
}