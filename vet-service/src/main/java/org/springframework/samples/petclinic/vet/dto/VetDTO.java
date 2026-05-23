package org.springframework.samples.petclinic.vet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object for Vet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representação de um veterinário")
public class VetDTO implements Serializable {

    @Schema(description = "ID único do veterinário", example = "1")
    private Integer id;

    @Schema(description = "Primeiro nome", example = "James")
    private String firstName;

    @Schema(description = "Último nome", example = "Carter")
    private String lastName;

    @Schema(description = "Lista de especialidades do veterinário")
    private List<SpecialtyDTO> specialties;
}
