package org.springframework.samples.petclinic.customer.dto;

import org.springframework.samples.petclinic.customer.domain.Pet;
import org.springframework.samples.petclinic.customer.domain.PetStatus;

public record PetValidationResponse(
        Long id,
        Long ownerId,
        PetStatus status
) {

    public static PetValidationResponse from(Pet pet) {
        return new PetValidationResponse(
                pet.getId(),
                pet.getOwner().getId(),
                pet.getStatus()
        );
    }
}
