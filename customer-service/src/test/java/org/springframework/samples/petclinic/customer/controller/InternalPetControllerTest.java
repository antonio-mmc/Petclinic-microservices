package org.springframework.samples.petclinic.customer.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customer.domain.Owner;
import org.springframework.samples.petclinic.customer.domain.Pet;
import org.springframework.samples.petclinic.customer.domain.PetStatus;
import org.springframework.samples.petclinic.customer.domain.PetType;
import org.springframework.samples.petclinic.customer.exception.PetNotFoundException;
import org.springframework.samples.petclinic.customer.exception.PetStatusException;
import org.springframework.samples.petclinic.customer.repository.PetRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link InternalPetController} using MockMvc + MockBean.
 * Covers: GET pet by ID (200/404), validação de status (inactive → conflict).
 */
@WebMvcTest(InternalPetController.class)
@DisplayName("InternalPetController REST API Tests")
class InternalPetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetRepository petRepository;

    // ─── GET /api/internal/pets/{petId} ──────────────────────────────────────

    @Nested
    @DisplayName("GET /api/internal/pets/{petId}")
    class ValidatePet {

        @Test
        @DisplayName("should return HTTP 200 with validation response when pet is ACTIVE")
        void shouldReturnOk_whenPetIsActive() throws Exception {
            Pet activePet = buildPet(3L, PetStatus.ACTIVE);
            given(petRepository.findByIdWithOwnerAndType(3L)).willReturn(Optional.of(activePet));

            mockMvc.perform(get("/api/internal/pets/{petId}", 3L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(3)))
                    .andExpect(jsonPath("$.status", is("ACTIVE")));
        }

        @Test
        @DisplayName("should return HTTP 404 when pet does not exist")
        void shouldReturn404_whenPetNotFound() throws Exception {
            given(petRepository.findByIdWithOwnerAndType(99L)).willReturn(Optional.empty());

            mockMvc.perform(get("/api/internal/pets/{petId}", 99L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Pet Not Found")));
        }

        @Test
        @DisplayName("should return HTTP 409 when pet is INACTIVE (validação de status)")
        void shouldReturn409_whenPetIsInactive() throws Exception {
            Pet inactivePet = buildPet(7L, PetStatus.INACTIVE);
            given(petRepository.findByIdWithOwnerAndType(7L)).willReturn(Optional.of(inactivePet));

            mockMvc.perform(get("/api/internal/pets/{petId}", 7L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title", is("Pet Status Conflict")));
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Pet buildPet(Long id, PetStatus status) {
        PetType type = new PetType();
        type.setId(1L);
        type.setName("dog");

        Owner owner = Owner.builder()
                .id(1L).firstName("John").lastName("Doe")
                .address("1 Main St").city("Springfield").telephone("555000111")
                .pets(new ArrayList<>())
                .build();

        return Pet.builder()
                .id(id)
                .name("Rex")
                .birthDate(LocalDate.of(2020, 1, 1))
                .status(status)
                .type(type)
                .owner(owner)
                .build();
    }
}
