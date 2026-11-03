package org.springframework.samples.petclinic.service;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerUserLink;
import org.springframework.samples.petclinic.model.VideoConsultation;
import org.springframework.samples.petclinic.model.VideoConsultation.TriageLevel;
import org.springframework.samples.petclinic.model.VideoConsultation.TriagePriority;
import org.springframework.samples.petclinic.repository.OwnerUserLinkRepository;
import org.springframework.samples.petclinic.repository.VetAvailabilitySlotRepository;
import org.springframework.samples.petclinic.repository.VideoConsultationRepository;
import org.springframework.samples.petclinic.repository.springdatajpa.SpringDataVetRepository;
import org.springframework.samples.petclinic.rest.dto.EmergencyVideoConsultationConfirmationDto;
import org.springframework.samples.petclinic.rest.dto.EmergencyVideoConsultationRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Default implementation of {@link VideoConsultationService}.
 */
@Service
public class VideoConsultationServiceImpl implements VideoConsultationService {

    private static final Logger logger = LoggerFactory.getLogger(VideoConsultationServiceImpl.class);

    private final OwnerUserLinkRepository ownerUserLinkRepository;
    private final VetAvailabilitySlotRepository vetAvailabilitySlotRepository;
    private final VideoConsultationRepository videoConsultationRepository;
    private final SpringDataVetRepository vetRepository;

    public VideoConsultationServiceImpl(OwnerUserLinkRepository ownerUserLinkRepository,
                                        VetAvailabilitySlotRepository vetAvailabilitySlotRepository,
                                        VideoConsultationRepository videoConsultationRepository,
                                        SpringDataVetRepository vetRepository) {
        this.ownerUserLinkRepository = ownerUserLinkRepository;
        this.vetAvailabilitySlotRepository = vetAvailabilitySlotRepository;
        this.videoConsultationRepository = videoConsultationRepository;
        this.vetRepository = vetRepository;
    }

    @Override
    @Transactional
    public EmergencyVideoConsultationConfirmationDto bookEmergency(String username, EmergencyVideoConsultationRequestDto request) {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(request, "request must not be null");

        Owner owner = resolveVerifiedOwner(username);

        OffsetDateTime start = request.getStartDateTime();
        OffsetDateTime end = request.getEndDateTime();
        if (start == null || end == null || !end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDateTime must be after startDateTime");
        }

        int vetId = Objects.requireNonNull(request.getVetId(), "vetId must not be null");

        // Concurrency guard: lock the vet row for this booking attempt.
        // This ensures that two concurrent requests cannot both pass overlap checks.
        var vet = vetRepository.findWithLockById(vetId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vet not found"));

        boolean isAvailable = vetAvailabilitySlotRepository.existsSlotCovering(vetId, start, end);
        if (!isAvailable) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vet is not available for the requested time window");
        }

        if (!videoConsultationRepository.findOverlappingForVet(vetId, start, end).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Requested time window overlaps with an existing booking");
        }

        VideoConsultation consultation = new VideoConsultation();
        consultation.setOwner(owner);
        consultation.setVet(vet);
        consultation.setStartDateTime(start);
        consultation.setEndDateTime(end);

        // Map OpenAPI-generated enums to domain enums by name (same constants).
        TriageLevel triageLevel = TriageLevel.valueOf(request.getTriageLevel().getValue());
        consultation.setTriageLevel(triageLevel);
        consultation.setTriagePriority(derivePriority(triageLevel));
        consultation.setReason(request.getReason());

        VideoConsultation saved = videoConsultationRepository.save(consultation);

        logger.info("Booked emergency video consultation id={} ownerId={} vetId={} start={} end={} triageLevel={}",
            saved.getId(), owner.getId(), vetId, start, end, triageLevel);

        return new EmergencyVideoConsultationConfirmationDto()
            .id(saved.getId())
            .ownerId(owner.getId())
            .vetId(vetId)
            .startDateTime(saved.getStartDateTime())
            .endDateTime(saved.getEndDateTime())
            .triageLevel(request.getTriageLevel())
            .triagePriority(toDtoPriority(saved.getTriagePriority()))
            .status(EmergencyVideoConsultationConfirmationDto.StatusEnum.CONFIRMED)
            .createdAt(saved.getCreatedAt().atOffset(OffsetDateTime.now().getOffset()));
    }

    private Owner resolveVerifiedOwner(String username) {
        OwnerUserLink link = ownerUserLinkRepository.findVerifiedByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated user is not linked to a verified owner"));
        return link.getOwner();
    }

    private static TriagePriority derivePriority(TriageLevel level) {
        return switch (level) {
            case MILD -> TriagePriority.LOW;
            case MODERATE -> TriagePriority.MEDIUM;
            case SEVERE -> TriagePriority.HIGH;
        };
    }

    private static EmergencyVideoConsultationConfirmationDto.TriagePriorityEnum toDtoPriority(TriagePriority priority) {
        return switch (priority) {
            case LOW -> EmergencyVideoConsultationConfirmationDto.TriagePriorityEnum.LOW;
            case MEDIUM -> EmergencyVideoConsultationConfirmationDto.TriagePriorityEnum.MEDIUM;
            case HIGH -> EmergencyVideoConsultationConfirmationDto.TriagePriorityEnum.HIGH;
        };
    }
}
