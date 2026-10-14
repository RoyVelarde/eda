package pe.eda.capacitacion.mapper;

import org.springframework.stereotype.Component;
import pe.eda.capacitacion.client.AulaClient;
import pe.eda.capacitacion.dto.response.AulaCapacitacionResponse;
import pe.eda.capacitacion.entity.AulaCapacitacion;

@Component
public class AulaCapacitacionMapper {

    public AulaCapacitacionResponse toResponse(
            AulaCapacitacion asignacion,
            AulaClient.AulaResponse aula
    ) {
        return new AulaCapacitacionResponse(
                asignacion.getId(),
                asignacion.getCapacitacion().getId(),
                asignacion.getAulaId(),
                aula.codigo(),
                aula.tipo(),
                aula.capacidad(),
                asignacion.getActiva(),
                asignacion.getFechaAsignacion()
        );
    }
}
