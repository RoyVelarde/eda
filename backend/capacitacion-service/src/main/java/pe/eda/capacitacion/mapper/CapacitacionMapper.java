package pe.eda.capacitacion.mapper;

import org.springframework.stereotype.Component;
import pe.eda.capacitacion.dto.request.CapacitacionRequest;
import pe.eda.capacitacion.dto.response.CapacitacionResponse;
import pe.eda.capacitacion.entity.Capacitacion;

@Component
public class CapacitacionMapper {
    public Capacitacion toEntity(CapacitacionRequest request) {
        Capacitacion capacitacion = new Capacitacion();
        capacitacion.setNombre(request.nombre());
        capacitacion.setAnio(request.anio());
        capacitacion.setFechaInicio(request.fechaInicio());
        capacitacion.setFechaFin(request.fechaFin());
        return capacitacion;
    }

    public CapacitacionResponse toResponse(Capacitacion capacitacion) {
        return new CapacitacionResponse(
                capacitacion.getId(),
                capacitacion.getNombre(),
                capacitacion.getAnio(),
                capacitacion.getFechaInicio(),
                capacitacion.getFechaFin(),
                capacitacion.getEstado(),
                capacitacion.getFechaCreacion(),
                capacitacion.getFechaActualizacion()
        );
    }
}
