package org.springframework.samples.petclinic.visit.messaging;

import java.time.Instant;

/**
 * Domain event received from Customer Service via RabbitMQ when a pet is deactivated.
 * The visit-service consumes this event to invalidate future visits for the deactivated pet.
 */
public record PetDeactivatedEvent(Long petId, Instant timestamp) {
}
