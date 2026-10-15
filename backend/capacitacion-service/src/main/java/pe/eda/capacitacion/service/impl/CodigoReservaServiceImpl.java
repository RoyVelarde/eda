package pe.eda.capacitacion.service.impl;

import lombok.RequiredArgsConstructor;
import pe.eda.capacitacion.dto.request.CodigoReservaRequest;
import pe.eda.capacitacion.dto.response.CodigoReservaResponse;
import pe.eda.capacitacion.entity.CodigoReserva;
import pe.eda.capacitacion.exception.ReglaNegocioException;
import pe.eda.capacitacion.exception.RecursoNoEncontradoException;
import pe.eda.capacitacion.repository.CapacitacionRepository;
import pe.eda.capacitacion.repository.CodigoReservaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.eda.capacitacion.service.CodigoReservaService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CodigoReservaServiceImpl implements CodigoReservaService {
    private final CodigoReservaRepository codigoReservaRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public CodigoReservaResponse configurar(
            UUID capacitacionId,
            CodigoReservaRequest request
    ) {
        capacitacionRepository.findById(capacitacionId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe la capacitación indicada"
                        )
                );
        CodigoReserva codigoReserva = codigoReservaRepository
                .findByCapacitacionId(capacitacionId)
                .orElseGet(CodigoReserva::new);
        codigoReserva.setCapacitacionId(capacitacionId);
        codigoReserva.setCodigoHash(
                passwordEncoder.encode(request.codigo())
        );
        codigoReserva.setActivo(true);
        CodigoReserva guardado = codigoReservaRepository.save(codigoReserva);
        return convertirRespuesta(guardado);
    }

    @Transactional(readOnly = true)
    @Override
    public CodigoReservaResponse consultar(UUID capacitacionId) {
        CodigoReserva codigoReserva = codigoReservaRepository
                .findByCapacitacionId(capacitacionId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La capacitación no tiene un código de reserva configurado"
                        )
                );
        return convertirRespuesta(codigoReserva);
    }

    @Transactional
    @Override
    public void desactivar(UUID capacitacionId) {
        CodigoReserva codigoReserva = codigoReservaRepository
                .findByCapacitacionId(capacitacionId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La capacitación no tiene un código de reserva configurado"
                        )
                );
        if (!Boolean.TRUE.equals(codigoReserva.getActivo())) {
            throw new ReglaNegocioException(
                    "El código de reserva ya está desactivado"
            );
        }
        codigoReserva.setActivo(false);
        codigoReservaRepository.save(codigoReserva);
    }

    private CodigoReservaResponse convertirRespuesta(
            CodigoReserva codigoReserva
    ) {
        return new CodigoReservaResponse(
                codigoReserva.getId(),
                codigoReserva.getCapacitacionId(),
                Boolean.TRUE.equals(codigoReserva.getActivo()),
                codigoReserva.getFechaCreacion(),
                codigoReserva.getFechaActualizacion()
        );
    }
}
