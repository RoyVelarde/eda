package pe.eda.capacitacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.eda.capacitacion.entity.Capacitacion;
import pe.eda.capacitacion.enums.EstadoCapacitacion;

import java.util.List;
import java.util.UUID;

public interface CapacitacionRepository extends JpaRepository<Capacitacion, UUID> {
    List<Capacitacion> findAllByOrderByFechaInicioDesc();

    List<Capacitacion> findAllByAnioOrderByFechaInicioDesc(Integer anio);

    List<Capacitacion> findAllByEstadoOrderByFechaInicioDesc(EstadoCapacitacion estado);
}