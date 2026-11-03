package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.rest.dto.EmergencyVideoConsultationConfirmationDto;
import org.springframework.samples.petclinic.rest.dto.EmergencyVideoConsultationRequestDto;

/**
 * Service for booking emergency video consultations.
 */
public interface VideoConsultationService {

    /**
     * Books an emergency video consultation for the authenticated user.
     *
     * @param username authenticated username (must be linked to an owner)
     * @param request booking request
     * @return confirmation
     */
    EmergencyVideoConsultationConfirmationDto bookEmergency(String username, EmergencyVideoConsultationRequestDto request);
}
