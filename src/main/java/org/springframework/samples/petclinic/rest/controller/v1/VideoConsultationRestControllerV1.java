package org.springframework.samples.petclinic.rest.controller.v1;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.api.VideoConsultationsApi;
import org.springframework.samples.petclinic.rest.dto.EmergencyVideoConsultationConfirmationDto;
import org.springframework.samples.petclinic.rest.dto.EmergencyVideoConsultationRequestDto;
import org.springframework.samples.petclinic.service.VideoConsultationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;

/**
 * REST controller for emergency video consultations.
 */
@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class VideoConsultationRestControllerV1 implements VideoConsultationsApi {

    private final VideoConsultationService videoConsultationService;

    public VideoConsultationRestControllerV1(VideoConsultationService videoConsultationService) {
        this.videoConsultationService = videoConsultationService;
    }

    @Override
    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    public ResponseEntity<EmergencyVideoConsultationConfirmationDto> bookEmergencyVideoConsultation(
        @Valid EmergencyVideoConsultationRequestDto emergencyVideoConsultationRequestDto) {

        String username = currentUsername();
        EmergencyVideoConsultationConfirmationDto confirmation = videoConsultationService.bookEmergency(username, emergencyVideoConsultationRequestDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.newInstance()
            .path("/api/video-consultations/{id}")
            .buildAndExpand(confirmation.getId())
            .toUri());

        return new ResponseEntity<>(confirmation, headers, HttpStatus.CREATED);
    }

    private static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("No authenticated principal found");
        }
        return authentication.getName();
    }
}
