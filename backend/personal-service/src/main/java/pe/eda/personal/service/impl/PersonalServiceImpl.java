package pe.eda.personal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.eda.personal.dto.request.PersonalRequest;
import pe.eda.personal.dto.response.ImportacionResponse;
import pe.eda.personal.dto.response.PersonalResponse;
import pe.eda.personal.entity.Personal;
import pe.eda.personal.enums.NivelPersonal;
import pe.eda.personal.enums.TipoImportacion;
import pe.eda.personal.exception.RecursoDuplicadoException;
import pe.eda.personal.exception.RecursoNoEncontradoException;
import pe.eda.personal.excel.ExcelPersonalReader;
import pe.eda.personal.excel.ExcelValidationService;
import pe.eda.personal.excel.ResultadoValidacionExcel;
import pe.eda.personal.mapper.PersonalExcelMapper;
import pe.eda.personal.mapper.PersonalMapper;
import pe.eda.personal.repository.PersonalRepository;
import pe.eda.personal.service.PersonalService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PersonalServiceImpl implements PersonalService {
    private final PersonalRepository personalRepository;
    private final PersonalMapper personalMapper;
    private final ExcelValidationService excelValidationService;
    private final ExcelPersonalReader excelPersonalReader;
    private final PersonalExcelMapper personalExcelMapper;

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
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el personal con el identificador indicado"));
        return personalMapper.toResponse(personal);
    }

    @Override
    public PersonalResponse buscarPorDni(String dni) {
        Personal personal = personalRepository.findByDni(dni.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró personal con el DNI indicado"));
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

    @Override
    @Transactional
    public ImportacionResponse importarExcel(MultipartFile archivo, TipoImportacion nivel) throws IOException {
        ResultadoValidacionExcel validacion = excelValidationService.validarArchivo(archivo, nivel);
        if (!validacion.errores().isEmpty() || validacion.filasValidas() != validacion.totalFilas()) {
            return new ImportacionResponse(
                    archivo.getOriginalFilename(),
                    nivel.name(),
                    false,
                    validacion.totalFilas(),
                    validacion.filasValidas(),
                    validacion.filasConError(),
                    0,
                    0,
                    validacion.errores()
            );
        }

        List<List<String>> filas = excelPersonalReader.leerFilas(archivo);
        if (filas.size() <= 1) {
            return new ImportacionResponse(
                    archivo.getOriginalFilename(),
                    nivel.name(),
                    true,
                    0,
                    0,
                    0,
                    0,
                    0,
                    List.of()
            );
        }

        Set<String> dnisArchivo = new HashSet<>();
        List<String> errores = new ArrayList<>();
        int indiceDni = nivel == TipoImportacion.NIVEL_2 ? 3 : 4;
        for (int i = 1; i < filas.size(); i++) {
            List<String> fila = filas.get(i);
            if (fila.stream().allMatch(
                    valor -> valor == null || valor.isBlank())) {
                continue;
            }
            String dni = fila.get(indiceDni).trim();
            if (!dnisArchivo.add(dni)) {
                errores.add(
                        "Fila " + (i + 1)
                                + ": el DNI " + dni
                                + " está repetido en el archivo."
                );
            }
        }

        if (!errores.isEmpty()) {
            return new ImportacionResponse(
                    archivo.getOriginalFilename(),
                    nivel.name(),
                    false,
                    validacion.totalFilas(),
                    validacion.filasValidas() - errores.size(),
                    errores.size(),
                    0,
                    0,
                    errores
            );
        }
        int registrosNuevos = 0;
        int registrosActualizados = 0;
        for (int i = 1; i < filas.size(); i++) {
            List<String> fila = filas.get(i);
            if (fila.stream().allMatch(
                    valor -> valor == null || valor.isBlank())) {
                continue;
            }
            Personal datosExcel = personalExcelMapper.convertir(fila, nivel);
            Personal personal;
            var existente = personalRepository.findByDni(datosExcel.getDni());
            if (existente.isPresent()) {
                personal = existente.get();
                actualizarDatos(personal, datosExcel);
                registrosActualizados++;
            } else {
                personal = datosExcel;
                registrosNuevos++;
            }
            personalRepository.save(personal);
        }

        return new ImportacionResponse(
                archivo.getOriginalFilename(),
                nivel.name(),
                true,
                validacion.totalFilas(),
                validacion.filasValidas(),
                0,
                registrosNuevos,
                registrosActualizados,
                List.of()
        );
    }

    private void actualizarDatos(Personal existente, Personal datosExcel) {
        existente.setNivel(datosExcel.getNivel());
        existente.setApellidoPaterno(datosExcel.getApellidoPaterno());
        existente.setApellidoMaterno(datosExcel.getApellidoMaterno());
        existente.setNombres(datosExcel.getNombres());
        existente.setCelular(datosExcel.getCelular());
        existente.setCorreo(datosExcel.getCorreo());
        existente.setCargo(datosExcel.getCargo());
        existente.setSituacion(datosExcel.getSituacion());
        existente.setRanking(datosExcel.getRanking());
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}