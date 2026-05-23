package org.springframework.samples.petclinic.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.samples.petclinic.customer.domain.Owner;

public record OwnerRequest(

        @NotBlank(message = "First name must not be blank")
        @Size(max = 30, message = "First name must be 30 characters or fewer")
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        @Size(max = 30, message = "Last name must be 30 characters or fewer")
        String lastName,

        @NotBlank(message = "Address must not be blank")
        @Size(max = 255, message = "Address must be 255 characters or fewer")
        String address,

        @NotBlank(message = "City must not be blank")
        @Size(max = 80, message = "City must be 80 characters or fewer")
        String city,

        @NotBlank(message = "Telephone must not be blank")
        @Size(max = 20, message = "Telephone must be 20 characters or fewer")
        @Pattern(regexp = "\\+?[0-9 .()-]+", message = "Telephone must contain only digits and the symbols + . ( ) -")
        String telephone
) {

    public Owner toEntity() {
        return Owner.builder()
                .firstName(firstName)
                .lastName(lastName)
                .address(address)
                .city(city)
                .telephone(telephone)
                .build();
    }
}
