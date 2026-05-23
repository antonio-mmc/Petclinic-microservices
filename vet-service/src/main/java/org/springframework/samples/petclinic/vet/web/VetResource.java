package org.springframework.samples.petclinic.vet.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.vet.dto.SpecialtyDTO;
import org.springframework.samples.petclinic.vet.dto.VetDTO;
import org.springframework.samples.petclinic.vet.model.Vet;
import org.springframework.samples.petclinic.vet.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.vet.repository.VetRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vets")
@Tag(name = "Veterinarians", description = "Endpoints para gestão de veterinários")
@RequiredArgsConstructor
@Slf4j
public class VetResource {

    private final VetRepository vetRepository;
    private final SpecialtyRepository specialtyRepository;

    @GetMapping
    @Operation(summary = "Listar todos os veterinários")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    })
    public List<VetDTO> getAllVets() {
        log.info("REST request to get all Vets");
        return vetRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{vetId}")
    @Operation(summary = "Obter detalhes de um veterinário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Veterinário encontrado"),
        @ApiResponse(responseCode = "404", description = "Veterinário não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<VetDTO> getVetById(
            @Parameter(description = "ID do veterinário", required = true)
            @PathVariable Integer vetId) {
        log.info("REST request to get Vet : {}", vetId);
        return vetRepository.findById(vetId)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new VetNotFoundException(vetId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar um novo veterinário")
    public VetDTO createVet(@RequestBody VetDTO vetDTO) {
        log.info("REST request to create Vet: {} {}", vetDTO.getFirstName(), vetDTO.getLastName());
        Vet vet = new Vet();
        vet.setFirstName(vetDTO.getFirstName());
        vet.setLastName(vetDTO.getLastName());
        applySpecialties(vet, vetDTO.getSpecialties());
        return mapToDTO(vetRepository.save(vet));
    }

    @PutMapping("/{vetId}")
    @Operation(summary = "Atualizar um veterinário existente")
    public VetDTO updateVet(@PathVariable Integer vetId, @RequestBody VetDTO vetDTO) {
        log.info("REST request to update Vet : {}", vetId);
        Vet vet = vetRepository.findById(vetId)
                .orElseThrow(() -> new VetNotFoundException(vetId));
        vet.setFirstName(vetDTO.getFirstName());
        vet.setLastName(vetDTO.getLastName());
        vet.setSpecialties(new HashSet<>());
        applySpecialties(vet, vetDTO.getSpecialties());
        return mapToDTO(vetRepository.save(vet));
    }

    @DeleteMapping("/{vetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover um veterinário")
    public void deleteVet(@PathVariable Integer vetId) {
        log.info("REST request to delete Vet : {}", vetId);
        Vet vet = vetRepository.findById(vetId)
                .orElseThrow(() -> new VetNotFoundException(vetId));
        vetRepository.delete(vet);
    }

    private void applySpecialties(Vet vet, List<SpecialtyDTO> specialtyDTOs) {
        if (specialtyDTOs == null) return;
        for (SpecialtyDTO dto : specialtyDTOs) {
            if (dto.getId() != null) {
                specialtyRepository.findById(dto.getId()).ifPresent(vet::addSpecialty);
            }
        }
    }

    private VetDTO mapToDTO(Vet vet) {
        return VetDTO.builder()
                .id(vet.getId())
                .firstName(vet.getFirstName())
                .lastName(vet.getLastName())
                .specialties(vet.getSpecialties().stream()
                        .map(s -> SpecialtyDTO.builder().id(s.getId()).name(s.getName()).build())
                        .collect(Collectors.toList()))
                .build();
    }
}
