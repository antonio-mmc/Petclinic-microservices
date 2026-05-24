package org.springframework.samples.petclinic.visit.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.visit.domain.Visit;
import org.springframework.samples.petclinic.visit.repository.VisitRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PetEventConsumer}.
 *
 * <p>Validates the two execution paths described in the TO-BE architecture:
 * <ul>
 *   <li><b>deleteAll</b> — all visits for a deactivated pet are removed.</li>
 *   <li><b>skip</b>     — no deletion occurs when no visits exist for the pet.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PetEventConsumer — async handler for PetDeactivatedEvent")
class PetEventConsumerTest {

    @Mock
    private VisitRepository visitRepository;

    @InjectMocks
    private PetEventConsumer consumer;

    private PetDeactivatedEvent event;

    @BeforeEach
    void setUp() {
        event = new PetDeactivatedEvent(10L, Instant.now());
    }

    // ─── deleteAll path ───────────────────────────────────────────────────────

    @Test
    @DisplayName("should delete all visits (deleteAll) when pet has one visit")
    void shouldDeleteAllVisits_whenPetHasOneVisit() {
        Visit visit = Visit.builder()
                .id(1).petId(10).visitDate(LocalDate.now().plusDays(5))
                .description("Scheduled follow-up").build();

        given(visitRepository.findByPetId(10)).willReturn(List.of(visit));

        consumer.handlePetDeactivated(event);

        verify(visitRepository).deleteAll(List.of(visit));
    }

    @Test
    @DisplayName("should delete all visits (deleteAll) when pet has multiple visits")
    void shouldDeleteAllVisits_whenPetHasMultipleVisits() {
        Visit v1 = Visit.builder().id(1).petId(10)
                .visitDate(LocalDate.now().plusDays(3)).description("Check-up").build();
        Visit v2 = Visit.builder().id(2).petId(10)
                .visitDate(LocalDate.now().plusDays(10)).description("Vaccination").build();
        Visit v3 = Visit.builder().id(3).petId(10)
                .visitDate(LocalDate.now().plusDays(30)).description("Annual exam").build();

        List<Visit> visits = List.of(v1, v2, v3);
        given(visitRepository.findByPetId(10)).willReturn(visits);

        consumer.handlePetDeactivated(event);

        verify(visitRepository).deleteAll(visits);
        verify(visitRepository, times(1)).deleteAll(anyList());
    }

    @Test
    @DisplayName("should call findByPetId with the correct petId from event")
    void shouldCallFindByPetId_withCorrectPetId() {
        PetDeactivatedEvent eventForPet42 = new PetDeactivatedEvent(42L, Instant.now());
        given(visitRepository.findByPetId(42)).willReturn(Collections.emptyList());

        consumer.handlePetDeactivated(eventForPet42);

        verify(visitRepository).findByPetId(42);
    }

    // ─── skip path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should skip deletion (skip) when pet has no visits")
    void shouldSkipDeletion_whenPetHasNoVisits() {
        given(visitRepository.findByPetId(10)).willReturn(Collections.emptyList());

        consumer.handlePetDeactivated(event);

        verify(visitRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("should never call deleteAll when repository returns empty list")
    void shouldNeverCallDeleteAll_whenRepositoryReturnsEmptyList() {
        given(visitRepository.findByPetId(anyInt())).willReturn(List.of());

        consumer.handlePetDeactivated(event);

        verify(visitRepository, never()).deleteAll(anyList());
        verify(visitRepository).findByPetId(10);
    }
}
