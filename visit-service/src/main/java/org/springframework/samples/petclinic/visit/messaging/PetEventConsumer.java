package org.springframework.samples.petclinic.visit.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.visit.domain.Visit;
import org.springframework.samples.petclinic.visit.repository.VisitRepository;

import java.util.List;
import java.util.function.Consumer;

/**
 * Async consumer for domain events published by Customer Service via RabbitMQ.
 *
 * <p>When a {@link PetDeactivatedEvent} is received, this consumer deletes all visits
 * associated with the deactivated pet (deleteAll), or skips if no visits are found (skip).
 * Historical visit records are preserved by design — only the repository's deleteAll
 * is called on the retrieved list, which Spring Data resolves at runtime.
 *
 * <p>Bound to the Spring Cloud Stream binding {@code petEvents-in-0}.
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class PetEventConsumer {

    private final VisitRepository visitRepository;

    /**
     * Spring Cloud Stream function binding: petEvents-in-0 → pet-events exchange.
     */
    @Bean
    public Consumer<PetDeactivatedEvent> petEvents() {
        return this::handlePetDeactivated;
    }

    /**
     * Handles a PetDeactivatedEvent:
     * <ul>
     *   <li><b>deleteAll</b> — deletes all visits for the pet if any are found.</li>
     *   <li><b>skip</b>     — no-ops if no visits exist for the given petId.</li>
     * </ul>
     */
    public void handlePetDeactivated(PetDeactivatedEvent event) {
        Integer petId = event.petId().intValue();
        List<Visit> visits = visitRepository.findByPetId(petId);

        if (visits.isEmpty()) {
            log.info("[PetEventConsumer] No visits found for deactivated petId={}. Skipping deletion.", event.petId());
            return;
        }

        visitRepository.deleteAll(visits);
        log.info("[PetEventConsumer] Deleted {} visit(s) for deactivated petId={}", visits.size(), event.petId());
    }
}
