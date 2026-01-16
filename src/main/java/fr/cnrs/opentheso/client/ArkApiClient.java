package fr.cnrs.opentheso.client;

import fr.cnrs.opentheso.dto.ArkRequest;

import fr.cnrs.opentheso.dto.ArkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
@Slf4j
@Service
public class ArkApiClient {

    private final RestTemplate restTemplate;

    public ArkApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public ArkResponse createArk(ArkRequest request, String urlServerOpenArk, String apiKey) {

        // "http://localhost:8080/api/addArk";
        String url = urlServerOpenArk + "/addArk";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", apiKey);

            HttpEntity<ArkRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ArkResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, ArkResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode(); // Spring 6+
            String body = e.getResponseBodyAsString();

            if (statusCode.value() == 401) {
                log.warn("Clé API invalide : {}", body);
            } else {
                log.error("Erreur HTTP OpenArk {} : {}", statusCode.value(), body);
            }

            throw new ArkApiException("Clé API invalide : " + body);
        } catch (ResourceAccessException e) {
            // Serveur inaccessible
            log.error("Serveur OpenArk inaccessible : {}", url);
            throw new ArkApiException("Serveur OpenArk indisponible");

        } catch (Exception e) {
            log.error("Erreur technique OpenArk", e);
            throw new ArkApiException("Erreur technique lors de l'appel OpenArk");
        }
    }
}
