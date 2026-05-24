package org.springframework.samples.petclinic.vet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vet.dto.VetDTO;
import org.springframework.samples.petclinic.vet.model.Vet;
import org.springframework.samples.petclinic.vet.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.vet.repository.VetRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link VetResource} using MockMvc + MockBean.
 * Covers: GET all vets, GET by ID (200/404), POST create vet.
 */
@WebMvcTest(VetResource.class)
@DisplayName("VetResource REST API Tests")
class VetResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VetRepository vetRepository;

    @MockBean
    private SpecialtyRepository specialtyRepository;

    // ─── GET /api/vets ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/vets")
    class GetAllVets {

        @Test
        @DisplayName("should return HTTP 200 with list of vets")
        void shouldReturnAllVets_withHttp200() throws Exception {
            Vet vet = buildVet(1, "James", "Carter");
            given(vetRepository.findAll()).willReturn(List.of(vet));

            mockMvc.perform(get("/api/vets")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].firstName", is("James")))
                    .andExpect(jsonPath("$[0].lastName", is("Carter")));
        }
    }

    // ─── GET /api/vets/{vetId} ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/vets/{vetId}")
    class GetVetById {

        @Test
        @DisplayName("should return HTTP 200 with vet details when found")
        void shouldReturnVet_whenFound() throws Exception {
            Vet vet = buildVet(2, "Helen", "Leary");
            given(vetRepository.findById(2)).willReturn(Optional.of(vet));

            mockMvc.perform(get("/api/vets/{vetId}", 2)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(2)))
                    .andExpect(jsonPath("$.firstName", is("Helen")))
                    .andExpect(jsonPath("$.lastName", is("Leary")));
        }

        @Test
        @DisplayName("should return HTTP 404 when vet does not exist")
        void shouldReturn404_whenVetNotFound() throws Exception {
            given(vetRepository.findById(99)).willReturn(Optional.empty());

            mockMvc.perform(get("/api/vets/{vetId}", 99)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Vet Not Found")));
        }
    }

    // ─── POST /api/vets ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/vets")
    class CreateVet {

        @Test
        @DisplayName("should create vet with HTTP 201")
        void shouldCreateVet_withHttp201() throws Exception {
            Vet savedVet = buildVet(3, "Linda", "Douglas");
            given(vetRepository.save(any())).willReturn(savedVet);

            String requestBody = """
                    {
                        "firstName": "Linda",
                        "lastName": "Douglas",
                        "specialties": []
                    }
                    """;

            mockMvc.perform(post("/api/vets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.firstName", is("Linda")))
                    .andExpect(jsonPath("$.lastName", is("Douglas")));
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Vet buildVet(Integer id, String firstName, String lastName) {
        Vet vet = new Vet();
        vet.setId(id);
        vet.setFirstName(firstName);
        vet.setLastName(lastName);
        return vet;
    }
}
