package pe.eda.aulas.mapper;

import org.springframework.stereotype.Component;
import pe.eda.aulas.dto.response.AulaResponse;
import pe.eda.aulas.entity.Aula;

@Component
public class AulaMapper {
    public AulaResponse toResponse(Aula aula) {
        return new AulaResponse(
                aula.getId(),
                aula.getCodigo(),
                aula.getTipo(),
                aula.getCapacidad(),
                aula.getActiva(),
                aula.getFechaCreacion()
        );
    }
}