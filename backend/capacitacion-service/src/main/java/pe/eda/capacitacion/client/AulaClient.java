package pe.eda.capacitacion.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.eda.capacitacion.exception.AulaServicioException;

import java.util.UUID;

@Component
public class AulaClient {
    private final RestClient restClient;

    public AulaClient(
            RestClient.Builder builder,
            @Value("${app.services.aulas.url}") String aulasUrl
    ) {
        this.restClient = builder
                .baseUrl(aulasUrl)
                .build();
    }

    public AulaResponse buscarPorId(UUID aulaId, String token) {
        try {
            return restClient.get()
                    .uri("/api/v1/aulas/{id}", aulaId)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + token
                    )
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new AulaServicioException(
                                        "No fue posible consultar el aula. "
                                                + "Código HTTP: "
                                                + response.getStatusCode()
                                );
                            }
                    )
                    .body(AulaResponse.class);

        } catch (RestClientException ex) {
            throw new AulaServicioException(
                    "Error de comunicación con aulas-service",
                    ex
            );
        }
    }

    public record AulaResponse(
            UUID id,
            String codigo,
            String tipo,
            Integer capacidad,
            Boolean activa
    ) {
    }
}