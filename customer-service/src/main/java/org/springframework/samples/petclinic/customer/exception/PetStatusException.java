package org.springframework.samples.petclinic.customer.exception;

public class PetStatusException extends RuntimeException {

    public PetStatusException(String message) {
        super(message);
    }
}
