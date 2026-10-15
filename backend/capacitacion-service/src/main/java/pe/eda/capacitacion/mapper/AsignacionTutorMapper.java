package pe.eda.capacitacion.mapper;

import org.springframework.stereotype.Component;
import pe.eda.capacitacion.client.PersonalClient;
import pe.eda.capacitacion.dto.response.AsignacionTutorResponse;
import pe.eda.capacitacion.entity.AsignacionTutor;

import java.util.UUID;

@Component
public class AsignacionTutorMapper {
    public AsignacionTutorResponse toResponse(
            AsignacionTutor asignacion,
            PersonalClient.PersonalResponse personal
    ) {
        UUID capacitacionId = asignacion.getCapacitacion().getId();
        UUID aulaCapacitacionId = asignacion.getAulaCapacitacion().getId();
        return new AsignacionTutorResponse(
                asignacion.getId(),
                capacitacionId,
                aulaCapacitacionId,
                asignacion.getPersonalId(),
                personal.dni(),
                personal.apellidoPaterno(),
                personal.apellidoMaterno(),
                personal.nombres(),
                personal.cargo(),
                asignacion.getFuncion(),
                asignacion.getActiva(),
                asignacion.getFechaAsignacion()
        );
    }
}