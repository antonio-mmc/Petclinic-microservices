package org.springframework.samples.petclinic.vet.web;

public class VetNotFoundException extends RuntimeException {
    public VetNotFoundException(Integer vetId) {
        super("Veterinarian with id " + vetId + " not found");
    }
}
