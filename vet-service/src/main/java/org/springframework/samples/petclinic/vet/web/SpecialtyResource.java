package org.springframework.samples.petclinic.vet.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.vet.dto.SpecialtyDTO;
import org.springframework.samples.petclinic.vet.model.Specialty;
import org.springframework.samples.petclinic.vet.repository.SpecialtyRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Specialties", description = "Specialty management endpoints")
public class SpecialtyResource {

    private final SpecialtyRepository specialtyRepository;

    @GetMapping
    @Operation(summary = "List all specialties")
    public List<SpecialtyDTO> getAllSpecialties() {
        log.info("Fetching all specialties");
        return specialtyRepository.findAll().stream()
                .map(s -> SpecialtyDTO.builder().id(s.getId()).name(s.getName()).build())
                .toList();
    }

    @GetMapping("/{specialtyId}")
    @Operation(summary = "Get specialty by ID")
    public ResponseEntity<SpecialtyDTO> getSpecialtyById(@PathVariable Integer specialtyId) {
        log.info("Fetching specialty with id {}", specialtyId);
        return specialtyRepository.findById(specialtyId)
                .map(s -> SpecialtyDTO.builder().id(s.getId()).name(s.getName()).build())
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new SpecialtyNotFoundException(specialtyId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new specialty")
    public SpecialtyDTO createSpecialty(@RequestBody SpecialtyDTO dto) {
        log.info("Creating specialty: {}", dto.getName());
        Specialty specialty = new Specialty();
        specialty.setName(dto.getName());
        Specialty saved = specialtyRepository.save(specialty);
        return SpecialtyDTO.builder().id(saved.getId()).name(saved.getName()).build();
    }

    @PutMapping("/{specialtyId}")
    @Operation(summary = "Update an existing specialty")
    public SpecialtyDTO updateSpecialty(@PathVariable Integer specialtyId, @RequestBody SpecialtyDTO dto) {
        log.info("Updating specialty with id {}", specialtyId);
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new SpecialtyNotFoundException(specialtyId));
        specialty.setName(dto.getName());
        Specialty saved = specialtyRepository.save(specialty);
        return SpecialtyDTO.builder().id(saved.getId()).name(saved.getName()).build();
    }

    @DeleteMapping("/{specialtyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a specialty")
    public void deleteSpecialty(@PathVariable Integer specialtyId) {
        log.info("Deleting specialty with id {}", specialtyId);
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new SpecialtyNotFoundException(specialtyId));
        specialtyRepository.delete(specialty);
    }
}
