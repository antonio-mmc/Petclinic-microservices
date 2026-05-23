package org.springframework.samples.petclinic.customer.dto;

import org.springframework.samples.petclinic.customer.domain.Owner;

import java.util.List;

public record OwnerResponse(
        Long id,
        String firstName,
        String lastName,
        String address,
        String city,
        String telephone,
        List<PetSummary> pets
) {

    public static OwnerResponse from(Owner owner) {
        List<PetSummary> petSummaries = owner.getPets().stream()
                .map(PetSummary::from)
                .toList();

        return new OwnerResponse(
                owner.getId(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getAddress(),
                owner.getCity(),
                owner.getTelephone(),
                petSummaries
        );
    }
}
