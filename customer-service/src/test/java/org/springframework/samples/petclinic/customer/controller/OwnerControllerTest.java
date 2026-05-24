package org.springframework.samples.petclinic.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.samples.petclinic.customer.dto.PetSummary;
import org.springframework.samples.petclinic.customer.exception.OwnerNotFoundException;
import org.springframework.samples.petclinic.customer.service.CustomerService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link OwnerController} using MockMvc + MockBean.
 * Covers: GET owner (200/404), POST pet (201/400/404).
 */
@WebMvcTest(OwnerController.class)
@DisplayName("OwnerController REST API Tests")
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ─── GET /api/owners/{ownerId} ────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/owners/{ownerId}")
    class GetOwner {

        @Test
        @DisplayName("should return owner with HTTP 200 when found")
        void shouldReturnOwner_whenFound() throws Exception {
            Owner owner = new Owner();
            owner.setId(1L);
            owner.setFirstName("George");
            owner.setLastName("Franklin");
            owner.setAddress("110 W. Liberty St.");
            owner.setCity("Madison");
            owner.setTelephone("6085551023");
            owner.setPets(List.of());

            given(customerService.findOwnerById(1L)).willReturn(owner);

            mockMvc.perform(get("/api/owners/{ownerId}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.firstName", is("George")))
                    .andExpect(jsonPath("$.lastName", is("Franklin")));
        }

        @Test
        @DisplayName("should return HTTP 404 when owner not found")
        void shouldReturn404_whenOwnerNotFound() throws Exception {
            given(customerService.findOwnerById(99L))
                    .willThrow(new OwnerNotFoundException(99L));

            mockMvc.perform(get("/api/owners/{ownerId}", 99L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Owner Not Found")));
        }
    }

    // ─── POST /api/owners/{ownerId}/pets ──────────────────────────────────────

    @Nested
    @DisplayName("POST /api/owners/{ownerId}/pets")
    class CreatePet {

        @Test
        @DisplayName("should create pet with HTTP 201 when owner exists")
        void shouldCreatePet_whenOwnerExists() throws Exception {
            PetType petType = new PetType();
            petType.setId(1L);
            petType.setName("cat");

            Pet pet = Pet.builder()
                    .id(5L).name("Bastet")
                    .birthDate(LocalDate.of(2022, 3, 10))
                    .status(PetStatus.ACTIVE)
                    .type(petType)
                    .owner(buildOwner(1L))
                    .build();

            given(customerService.createPet(eq(1L), any())).willReturn(pet);

            String requestBody = """
                    {
                        "name": "Bastet",
                        "birthDate": "2022-03-10",
                        "typeId": 1
                    }
                    """;

            mockMvc.perform(post("/api/owners/{ownerId}/pets", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Bastet")))
                    .andExpect(jsonPath("$.status", is("ACTIVE")));
        }

        @Test
        @DisplayName("should return HTTP 400 when request body is invalid")
        void shouldReturn400_whenRequestBodyIsInvalid() throws Exception {
            String invalidBody = """
                    {
                        "birthDate": "2022-03-10",
                        "typeId": 1
                    }
                    """;
            // missing required 'name' field

            mockMvc.perform(post("/api/owners/{ownerId}/pets", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return HTTP 404 when owner does not exist")
        void shouldReturn404_whenOwnerNotFound() throws Exception {
            given(customerService.createPet(eq(99L), any()))
                    .willThrow(new OwnerNotFoundException(99L));

            String requestBody = """
                    {
                        "name": "Milo",
                        "birthDate": "2021-06-15",
                        "typeId": 2
                    }
                    """;

            mockMvc.perform(post("/api/owners/{ownerId}/pets", 99L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Owner Not Found")));
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Owner buildOwner(Long id) {
        Owner owner = new Owner();
        owner.setId(id);
        owner.setFirstName("Test");
        owner.setLastName("Owner");
        owner.setAddress("Street 1");
        owner.setCity("City");
        owner.setTelephone("123456789");
        owner.setPets(List.of());
        return owner;
    }
}
