package org.springframework.samples.petclinic.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.customer.domain.Pet;

import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    @Query("""
            SELECT p FROM Pet p
            JOIN FETCH p.owner
            JOIN FETCH p.type
            WHERE p.id = :id
            """)
    Optional<Pet> findByIdWithOwnerAndType(@Param("id") Long id);
}
