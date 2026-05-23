package org.springframework.samples.petclinic.vet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Models a {@link Vet Vet's} specialty (for example, dentistry).
 */
@Entity
@Table(name = "specialties")
@Getter
@Setter
public class Specialty extends BaseEntity {

    @Column(name = "name")
    private String name;

}
