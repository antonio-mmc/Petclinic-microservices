package org.springframework.samples.petclinic.vet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.vet.model.Vet;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository class for <code>Vet</code> domain objects All method names are compliant with Spring Data naming
 * conventions so this interface can easily be extended for Spring Data 
 */
public interface VetRepository extends JpaRepository<Vet, Integer> {

    /**
     * Retrieve all <code>Vet</code>s from the data store with their specialties eagerly loaded.
     */
    @EntityGraph(attributePaths = {"specialties"})
    @Transactional(readOnly = true)
    Page<Vet> findAll(Pageable pageable);

    /**
     * Retrieve a <code>Vet</code> by id with their specialties eagerly loaded.
     */
    @EntityGraph(attributePaths = {"specialties"})
    @Transactional(readOnly = true)
    Optional<Vet> findById(Integer id);
}
