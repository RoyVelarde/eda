package pe.eda.personal.mapper;

import org.springframework.stereotype.Component;
import pe.eda.personal.entity.Personal;
import pe.eda.personal.enums.NivelPersonal;
import pe.eda.personal.enums.TipoImportacion;

import java.util.List;
import java.util.UUID;

@Component
public class PersonalExcelMapper {

    public Personal convertir(List<String> fila, TipoImportacion tipo) {
        Personal personal = new Personal();
        int indiceApellidoPaterno;
        int indiceApellidoMaterno;
        int indiceNombres;
        int indiceDni;
        int indiceCelular;
        int indiceCorreo;
        int indiceCargo;
        int indiceSituacion;
        if (tipo == TipoImportacion.NIVEL_2) {
            indiceApellidoPaterno = 0;
            indiceApellidoMaterno = 1;
            indiceNombres = 2;
            indiceDni = 3;
            indiceCelular = 4;
            indiceCorreo = 5;
            indiceCargo = 6;
            indiceSituacion = -1;
            personal.setRanking(null);
        } else {
            indiceApellidoPaterno = 1;
            indiceApellidoMaterno = 2;
            indiceNombres = 3;
            indiceDni = 4;
            indiceCorreo = 5;
            indiceCelular = 6;
            indiceCargo = 7;
            indiceSituacion = 8;
            String ranking = obtener(fila, 0);
            assert ranking != null;
            personal.setRanking(Integer.parseInt(ranking));
        }
        personal.setId(UUID.randomUUID());
        personal.setNivel(
                tipo == TipoImportacion.NIVEL_2
                        ? NivelPersonal.NIVEL_2
                        : NivelPersonal.NIVEL_3
        );
        personal.setApellidoPaterno(obtener(fila, indiceApellidoPaterno));
        personal.setApellidoMaterno(obtener(fila, indiceApellidoMaterno));
        personal.setNombres(obtener(fila, indiceNombres));
        personal.setDni(obtener(fila, indiceDni));
        personal.setCelular(obtener(fila, indiceCelular));
        personal.setCorreo(obtener(fila, indiceCorreo));
        personal.setCargo(obtener(fila, indiceCargo));
        personal.setSituacion(
                indiceSituacion >= 0
                        ? obtener(fila, indiceSituacion)
                        : null
        );
        return personal;
    }

    private String obtener(List<String> fila, int indice) {
        if (indice < 0 || indice >= fila.size()) {
            return null;
        }
        String valor = fila.get(indice);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
