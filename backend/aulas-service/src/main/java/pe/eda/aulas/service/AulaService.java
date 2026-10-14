package pe.eda.aulas.service;

import pe.eda.aulas.dto.request.AulaRequest;
import pe.eda.aulas.dto.response.AulaResponse;
import pe.eda.aulas.enums.TipoAula;

import java.util.List;
import java.util.UUID;

public interface AulaService {
    AulaResponse registrar(AulaRequest request);

    AulaResponse buscarPorId(UUID id);

    AulaResponse buscarPorCodigo(String codigo);

    List<AulaResponse> listarTodas();

    List<AulaResponse> listarPorTipo(TipoAula tipo);

    List<AulaResponse> listarActivas();
}
