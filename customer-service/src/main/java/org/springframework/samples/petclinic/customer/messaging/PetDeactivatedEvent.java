package org.springframework.samples.petclinic.customer.messaging;

import java.time.Instant;

public record PetDeactivatedEvent(Long petId, Instant timestamp) {

    public static PetDeactivatedEvent of(Long petId) {
        return new PetDeactivatedEvent(petId, Instant.now());
    }
}
