package org.springframework.samples.petclinic.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PetRequest(

        @NotBlank(message = "Pet name must not be blank")
        @Size(max = 30, message = "Pet name must be 30 characters or fewer")
        String name,

        @NotNull(message = "Birth date must not be null")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @NotNull(message = "Type id must not be null")
        Long typeId
) {

    public static PetRequest of(String name, LocalDate birthDate, Long typeId) {
        return new PetRequest(name, birthDate, typeId);
    }
}
