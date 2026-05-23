package org.springframework.samples.petclinic.visit.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.visit.dto.PetValidationResponse;

@Service
@Slf4j
public class PetValidationClient {

    private final RestClient restClient;

    public PetValidationClient(@Value("${app.customer-service.url:http://localhost:8080}") String customerServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(customerServiceUrl).build();
    }

    @CircuitBreaker(name = "petService", fallbackMethod = "handlePetValidationFallback")
    public void validatePet(Integer petId) {
        log.info("Validating pet {} with Customer Service", petId);

        PetValidationResponse response = restClient.get()
                .uri("/api/internal/pets/{petId}", petId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {
                    throw new PetValidationException("Pet with ID " + petId + " does not exist.");
                })
                .onStatus(status -> status.value() == 409, (req, res) -> {
                    throw new InactivePetException(petId);
                })
                .body(PetValidationResponse.class);

        if (response != null && "INACTIVE".equals(response.status())) {
            throw new InactivePetException(petId);
        }
    }

    // Business exceptions (PetValidationException, InactivePetException) are re-thrown so that
    // GlobalExceptionHandler maps them to the correct HTTP status (400/409).
    // Only infrastructure failures (network down, timeout) become RemoteServiceException (503).
    public void handlePetValidationFallback(Integer petId, Exception ex) {
        if (ex instanceof PetValidationException || ex instanceof InactivePetException) {
            throw (RuntimeException) ex;
        }
        log.error("Fallback triggered for petId {}. Reason: {}", petId, ex.getMessage());
        throw new RemoteServiceException("Customer Service is currently unavailable or too slow. Please try again later.", ex);
    }
}
