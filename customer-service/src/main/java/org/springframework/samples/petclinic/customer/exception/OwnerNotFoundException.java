package org.springframework.samples.petclinic.customer.exception;

public class OwnerNotFoundException extends RuntimeException {

    public OwnerNotFoundException(Long ownerId) {
        super("Owner not found with id: " + ownerId);
    }
}
