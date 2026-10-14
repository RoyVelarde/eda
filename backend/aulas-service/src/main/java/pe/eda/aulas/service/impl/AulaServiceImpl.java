package pe.eda.aulas.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.eda.aulas.dto.request.AulaRequest;
import pe.eda.aulas.dto.response.AulaResponse;
import pe.eda.aulas.entity.Aula;
import pe.eda.aulas.enums.TipoAula;
import pe.eda.aulas.exception.RecursoDuplicadoException;
import pe.eda.aulas.exception.RecursoNoEncontradoException;
import pe.eda.aulas.mapper.AulaMapper;
import pe.eda.aulas.repository.AulaRepository;
import pe.eda.aulas.service.AulaService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AulaServiceImpl implements AulaService {
    private final AulaRepository aulaRepository;
    private final AulaMapper aulaMapper;

    @Override
    @Transactional
    public AulaResponse registrar(AulaRequest request) {
        String codigo = request.codigo()
                .trim()
                .toUpperCase(Locale.ROOT);
        if (aulaRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new RecursoDuplicadoException(
                    "Ya existe un aula registrada con el código " + codigo
            );
        }
        Aula aula = new Aula();
        aula.setCodigo(codigo);
        aula.setTipo(request.tipo());
        aula.setCapacidad(request.capacidad());
        aula.setActiva(true);
        Aula guardada = aulaRepository.save(aula);
        return aulaMapper.toResponse(guardada);
    }

    @Override
    public AulaResponse buscarPorId(UUID id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el aula con el identificador indicado"
                ));
        return aulaMapper.toResponse(aula);
    }

    @Override
    public AulaResponse buscarPorCodigo(String codigo) {
        String codigoNormalizado = codigo.trim()
                .toUpperCase(Locale.ROOT);
        Aula aula = aulaRepository.findByCodigoIgnoreCase(codigoNormalizado)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el aula con el código indicado"
                ));
        return aulaMapper.toResponse(aula);
    }

    @Override
    public List<AulaResponse> listarTodas() {
        return aulaRepository.findAllByOrderByCodigoAsc()
                .stream()
                .map(aulaMapper::toResponse)
                .toList();
    }

    @Override
    public List<AulaResponse> listarPorTipo(TipoAula tipo) {
        return aulaRepository.findAllByTipoOrderByCodigoAsc(tipo)
                .stream()
                .map(aulaMapper::toResponse)
                .toList();
    }

    @Override
    public List<AulaResponse> listarActivas() {
        return aulaRepository.findAllByActivaTrueOrderByCodigoAsc()
                .stream()
                .map(aulaMapper::toResponse)
                .toList();
    }
}