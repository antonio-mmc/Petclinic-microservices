package org.springframework.samples.petclinic.vet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object for Specialty.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representação de uma especialidade veterinária")
public class SpecialtyDTO implements Serializable {

    @Schema(description = "ID único da especialidade", example = "1")
    private Integer id;

    @Schema(description = "Nome da especialidade", example = "radiology")
    private String name;
}
