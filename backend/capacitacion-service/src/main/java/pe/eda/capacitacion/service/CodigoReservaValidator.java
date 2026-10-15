package pe.eda.capacitacion.service;

import java.util.UUID;

public interface CodigoReservaValidator {

    void validar(UUID capacitacionId, String codigoIngresado);

}
