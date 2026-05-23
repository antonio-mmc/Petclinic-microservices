package org.springframework.samples.petclinic.customer.exception;

public class PetNotFoundException extends RuntimeException {

    public PetNotFoundException(Long petId) {
        super("Pet not found with id: " + petId);
    }
}
