package pe.eda.capacitacion.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.eda.capacitacion.exception.PersonalServicioException;

import java.util.UUID;

@Component
public class PersonalClient {
    private final RestClient restClient;

    public PersonalClient(
            RestClient.Builder builder,
            @Value("${app.services.personal.url}") String personalUrl
    ) {
        this.restClient = builder
                .baseUrl(personalUrl)
                .build();
    }

    public PersonalResponse buscarPorId(UUID personalId, String token) {
        try {
            return restClient.get()
                    .uri("/api/v1/personal/{id}", personalId)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + token
                    )
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new PersonalServicioException(
                                "No fue posible consultar al personal. Código HTTP: "
                                        + response.getStatusCode()
                        );
                    })
                    .body(PersonalResponse.class);
        } catch (RestClientException ex) {
            throw new PersonalServicioException(
                    "Error de comunicación con personal-service.",
                    ex
            );
        }
    }

    public PersonalResponse buscarPorDni(String dni, String token) {
        try {
            return restClient.get()
                    .uri("/api/v1/personal/dni/{dni}", dni)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + token
                    )
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new pe.eda.capacitacion.exception.RecursoNoEncontradoException(
                                        "No se encontró ningún participante con el DNI indicado."
                                );
                            }
                    )
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new PersonalServicioException(
                                        "No fue posible consultar al personal por DNI. Código HTTP: "
                                                + response.getStatusCode()
                                );
                            }
                    )
                    .body(PersonalResponse.class);
        } catch (PersonalServicioException | pe.eda.capacitacion.exception.RecursoNoEncontradoException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new PersonalServicioException(
                    "Error de comunicación con personal-service.",
                    ex
            );
        }
    }

    public record PersonalResponse(
            UUID id,
            String dni,
            String nivel,
            String apellidoPaterno,
            String apellidoMaterno,
            String nombres,
            String cargo,
            String situacion,
            Integer ranking
    ) {
    }
}
