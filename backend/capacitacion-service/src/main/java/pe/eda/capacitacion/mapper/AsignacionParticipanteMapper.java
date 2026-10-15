package pe.eda.capacitacion.mapper;

import org.springframework.stereotype.Component;
import pe.eda.capacitacion.client.AulaClient;
import pe.eda.capacitacion.client.PersonalClient;
import pe.eda.capacitacion.dto.response.AsignacionParticipanteResponse;
import pe.eda.capacitacion.entity.AsignacionParticipante;

@Component
public class AsignacionParticipanteMapper {

    public AsignacionParticipanteResponse toResponse(
            AsignacionParticipante asignacion,
            PersonalClient.PersonalResponse personal,
            AulaClient.AulaResponse aula
    ) {
        return new AsignacionParticipanteResponse(
                asignacion.getId(),
                asignacion.getCapacitacion().getId(),
                asignacion.getPersonalId(),
                personal.dni(),
                personal.apellidoPaterno(),
                personal.apellidoMaterno(),
                personal.nombres(),
                personal.cargo(),
                asignacion.getFuncion(),
                personal.situacion(),
                personal.ranking(),
                asignacion.getAulaCapacitacion().getId(),
                aula.id(),
                aula.codigo(),
                aula.tipo(),
                asignacion.getFechaAsignacion()
        );
    }
}
