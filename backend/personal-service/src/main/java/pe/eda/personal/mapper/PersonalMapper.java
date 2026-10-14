package pe.eda.personal.mapper;

import org.springframework.stereotype.Component;

import pe.eda.personal.dto.response.PersonalResponse;
import pe.eda.personal.entity.Personal;

@Component
public class PersonalMapper {

    public PersonalResponse toResponse(Personal personal) {
        return new PersonalResponse(
                personal.getId(),
                personal.getDni(),
                personal.getNivel(),
                personal.getApellidoPaterno(),
                personal.getApellidoMaterno(),
                personal.getNombres(),
                personal.getCelular(),
                personal.getCorreo(),
                personal.getCargo(),
                personal.getSituacion(),
                personal.getRanking(),
                personal.getFechaCreacion()
        );
    }
}