package pe.eda.personal.repository;

import pe.eda.personal.entity.Personal;
import pe.eda.personal.enums.NivelPersonal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonalRepository extends JpaRepository<Personal, UUID> {

    Optional<Personal> findByDni(String dni);

    boolean existsByDni(String dni);

    List<Personal> findAllByNivel(NivelPersonal nivel);

    List<Personal> findAllByNivelOrderByApellidoPaternoAscApellidoMaternoAscNombresAsc(NivelPersonal nivel);
}