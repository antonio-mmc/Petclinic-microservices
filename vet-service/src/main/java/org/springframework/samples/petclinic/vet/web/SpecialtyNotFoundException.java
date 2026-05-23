package org.springframework.samples.petclinic.vet.web;

public class SpecialtyNotFoundException extends RuntimeException {
    public SpecialtyNotFoundException(Integer id) {
        super("Specialty not found with id: " + id);
    }
}
