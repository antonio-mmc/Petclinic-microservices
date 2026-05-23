package org.springframework.samples.petclinic.vet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.vet.model.Specialty;

public interface SpecialtyRepository extends JpaRepository<Specialty, Integer> {
}
