package pe.eda.personal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.eda.personal.dto.request.PersonalRequest;
import pe.eda.personal.dto.response.PersonalResponse;
import pe.eda.personal.entity.Personal;
import pe.eda.personal.enums.NivelPersonal;
import pe.eda.personal.exception.RecursoDuplicadoException;
import pe.eda.personal.exception.RecursoNoEncontradoException;
import pe.eda.personal.mapper.PersonalMapper;
import pe.eda.personal.repository.PersonalRepository;
import pe.eda.personal.service.PersonalService;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PersonalServiceImpl implements PersonalService {
    private final PersonalRepository personalRepository;
    private final PersonalMapper personalMapper;

    @Override
    @Transactional
    public PersonalResponse registrar(PersonalRequest request) {
        String dni = request.dni().trim();
        if (personalRepository.existsByDni(dni)) {
            throw new RecursoDuplicadoException("Ya existe una persona registrada con ese DNI");
        }
        Personal personal = new Personal();
        personal.setDni(dni);
        personal.setNivel(request.nivel());
        personal.setApellidoPaterno(request.apellidoPaterno().trim());
        personal.setApellidoMaterno(request.apellidoMaterno().trim());
        personal.setNombres(request.nombres().trim());
        personal.setCelular(normalizarOpcional(request.celular()));
        personal.setCorreo(normalizarOpcional(request.correo()));
        personal.setCargo(request.cargo().trim());
        personal.setSituacion(normalizarOpcional(request.situacion()));
        Personal guardado = personalRepository.save(personal);
        return personalMapper.toResponse(guardado);
    }

    @Override
    public PersonalResponse buscarPorId(UUID id) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el personal con el identificador indicado"
                ));
        return personalMapper.toResponse(personal);
    }

    @Override
    public PersonalResponse buscarPorDni(String dni) {
        Personal personal = personalRepository.findByDni(dni.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró personal con el DNI indicado"
                ));
        return personalMapper.toResponse(personal);
    }

    @Override
    public List<PersonalResponse> listarTodos() {
        return personalRepository.findAll()
                .stream()
                .map(personalMapper::toResponse)
                .toList();
    }

    @Override
    public List<PersonalResponse> listarPorNivel(NivelPersonal nivel) {
        return personalRepository
                .findAllByNivelOrderByApellidoPaternoAscApellidoMaternoAscNombresAsc(nivel)
                .stream()
                .map(personalMapper::toResponse)
                .toList();
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}