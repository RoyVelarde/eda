package pe.eda.capacitacion.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.eda.capacitacion.entity.CodigoReserva;
import pe.eda.capacitacion.exception.ReglaNegocioException;
import pe.eda.capacitacion.repository.CodigoReservaRepository;
import pe.eda.capacitacion.service.CodigoReservaValidator;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CodigoReservaValidatorImpl implements CodigoReservaValidator {
    private final CodigoReservaRepository codigoReservaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public void validar(UUID capacitacionId, String codigoIngresado) {
        CodigoReserva configuracion = codigoReservaRepository
                .findByCapacitacionId(capacitacionId)
                .orElseThrow(() ->
                        new ReglaNegocioException(
                                "Esta capacitación no tiene un código de reserva configurado."
                        )
                );
        if (!Boolean.TRUE.equals(configuracion.getActivo())) {
            throw new ReglaNegocioException(
                    "El código de reserva de esta capacitación está desactivado."
            );
        }
        if (codigoIngresado == null
                || codigoIngresado.isBlank()
                || !passwordEncoder.matches(
                codigoIngresado,
                configuracion.getCodigoHash()
        )) {
            throw new ReglaNegocioException(
                    "El código de reserva es incorrecto."
            );
        }
    }
}