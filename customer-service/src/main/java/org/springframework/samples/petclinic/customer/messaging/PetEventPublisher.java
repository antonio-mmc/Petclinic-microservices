package org.springframework.samples.petclinic.customer.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PetEventPublisher {

    private static final String PET_EVENTS_BINDING = "petEvents-out-0";

    private final StreamBridge streamBridge;

    public void publishPetDeactivatedEvent(Long petId) {
        PetDeactivatedEvent event = PetDeactivatedEvent.of(petId);
        streamBridge.send(PET_EVENTS_BINDING, event);
        log.info("Published PetDeactivatedEvent for petId={} at {}", event.petId(), event.timestamp());
    }
}
