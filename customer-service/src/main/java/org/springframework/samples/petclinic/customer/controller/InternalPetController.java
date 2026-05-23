package org.springframework.samples.petclinic.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.customer.domain.Pet;
import org.springframework.samples.petclinic.customer.domain.PetStatus;
import org.springframework.samples.petclinic.customer.dto.PetValidationResponse;
import org.springframework.samples.petclinic.customer.exception.PetNotFoundException;
import org.springframework.samples.petclinic.customer.exception.PetStatusException;
import org.springframework.samples.petclinic.customer.repository.PetRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Tag(name = "Internal", description = "Internal service-to-service endpoints")
public class InternalPetController {

    private final PetRepository petRepository;

    @GetMapping("/pets/{petId}")
    @Operation(summary = "Validate a pet for inter-service use (returns owner id and status)")
    public ResponseEntity<PetValidationResponse> validatePet(@PathVariable Long petId) {
        Pet pet = petRepository.findByIdWithOwnerAndType(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));

        if (pet.getStatus() == PetStatus.INACTIVE) {
            throw new PetStatusException("Pet with id " + petId + " is INACTIVE and cannot be used");
        }

        return ResponseEntity.ok(PetValidationResponse.from(pet));
    }
}
