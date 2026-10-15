package pe.eda.capacitacion.service;

import pe.eda.capacitacion.dto.request.CodigoReservaRequest;
import pe.eda.capacitacion.dto.response.CodigoReservaResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface CodigoReservaService {
    CodigoReservaResponse configurar(UUID capacitacionId, CodigoReservaRequest request);

    CodigoReservaResponse consultar(UUID capacitacionId);

    void desactivar(UUID capacitacionId);
}