package org.springframework.samples.petclinic.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.rest.controller.v1.VideoConsultationRestControllerV1;
import org.springframework.samples.petclinic.rest.dto.EmergencyVideoConsultationRequestDto;
import org.springframework.samples.petclinic.service.VideoConsultationService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tools.jackson.databind.ObjectMapper;

/**
 * GENERATED TESTS (not executed here): Controller-level tests for JIRA-402.
 */
@SpringBootTest
@ActiveProfiles({"spring-data-jpa", "hsqldb"})
class VideoConsultationRestControllerV1Tests {

    @Autowired
    private VideoConsultationRestControllerV1 controller;

    @Autowired
    private VideoConsultationService videoConsultationService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ExceptionControllerAdvice())
            .build();
    }

    @Test
    @WithMockUser(username = "owner1", roles = "OWNER_ADMIN")
    void shouldBookEmergencyVideoConsultationAndReturnConfirmation() throws Exception {
        EmergencyVideoConsultationRequestDto request = new EmergencyVideoConsultationRequestDto()
            .vetId(1)
            .startDateTime(OffsetDateTime.parse("2026-11-03T10:00:00Z"))
            .endDateTime(OffsetDateTime.parse("2026-11-03T10:15:00Z"))
            .triageLevel(EmergencyVideoConsultationRequestDto.TriageLevelEnum.SEVERE)
            .reason("Breathing difficulty");

        mockMvc.perform(post("/api/video-consultations/emergency")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.ownerId").value(1))
            .andExpect(jsonPath("$.vetId").value(1))
            .andExpect(jsonPath("$.triageLevel").value("SEVERE"))
            .andExpect(jsonPath("$.triagePriority").value("HIGH"))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "owner1", roles = "OWNER_ADMIN")
    void shouldRejectOverlappingBooking() throws Exception {
        EmergencyVideoConsultationRequestDto request1 = new EmergencyVideoConsultationRequestDto()
            .vetId(1)
            .startDateTime(OffsetDateTime.parse("2026-11-03T11:00:00Z"))
            .endDateTime(OffsetDateTime.parse("2026-11-03T11:15:00Z"))
            .triageLevel(EmergencyVideoConsultationRequestDto.TriageLevelEnum.MILD)
            .reason("Minor concern");

        EmergencyVideoConsultationRequestDto request2 = new EmergencyVideoConsultationRequestDto()
            .vetId(1)
            .startDateTime(OffsetDateTime.parse("2026-11-03T11:05:00Z"))
            .endDateTime(OffsetDateTime.parse("2026-11-03T11:20:00Z"))
            .triageLevel(EmergencyVideoConsultationRequestDto.TriageLevelEnum.MODERATE)
            .reason("Overlapping concern");

        // First booking succeeds
        mockMvc.perform(post("/api/video-consultations/emergency")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());

        // Second booking conflicts
        mockMvc.perform(post("/api/video-consultations/emergency")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "unlinked", roles = "OWNER_ADMIN")
    void shouldRejectWhenUserNotLinkedToOwner() throws Exception {
        EmergencyVideoConsultationRequestDto request = new EmergencyVideoConsultationRequestDto()
            .vetId(1)
            .startDateTime(OffsetDateTime.parse("2026-11-03T12:00:00Z"))
            .endDateTime(OffsetDateTime.parse("2026-11-03T12:15:00Z"))
            .triageLevel(EmergencyVideoConsultationRequestDto.TriageLevelEnum.SEVERE)
            .reason("Emergency");

        mockMvc.perform(post("/api/video-consultations/emergency")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}
