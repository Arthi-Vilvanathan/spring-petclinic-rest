/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.samples.petclinic.repository.springdatajpa.PrescriptionRefillRepository;
import org.springframework.samples.petclinic.repository.springdatajpa.PrescriptionRepository;
import org.springframework.samples.petclinic.rest.dto.*;
import org.springframework.samples.petclinic.rest.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Prescription business logic.
 * Implements workflow integration with quality gates and HITL approval.
 *
 * @author PetClinic Domain Advisor
 */
@Service
@Transactional
public class PrescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionService.class);
    private static final int MAX_REGENERATION_ATTEMPTS = 2;

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionRefillRepository refillRepository;
    private final VisitRepository visitRepository;
    private final PetRepository petRepository;
    private final VetRepository vetRepository;

    public PrescriptionService(
        PrescriptionRepository prescriptionRepository,
        PrescriptionRefillRepository refillRepository,
        VisitRepository visitRepository,
        PetRepository petRepository,
        VetRepository vetRepository
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.refillRepository = refillRepository;
        this.visitRepository = visitRepository;
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
    }

    /**
     * Create a new prescription with workflow validation
     */
    public PrescriptionResponse createPrescription(PrescriptionCreateRequest request) throws DataAccessException {
        logger.info("Creating prescription for pet ID: {}", request.petId());

        // Validate visit exists
        Visit visit = visitRepository.findById(request.visitId());
        if (visit == null) {
            throw new ResourceNotFoundException("Visit", "id", request.visitId());
        }

        // Validate pet exists
        Pet pet = petRepository.findById(request.petId());
        if (pet == null) {
            throw new ResourceNotFoundException("Pet", "id", request.petId());
        }

        // Validate veterinarian exists
        Vet vet = vetRepository.findById(request.veterinarianId());
        if (vet == null) {
            throw new ResourceNotFoundException("Veterinarian", "id", request.veterinarianId());
        }

        // Create prescription entity
        Prescription prescription = new Prescription();
        prescription.setVisit(visit);
        prescription.setPet(pet);
        prescription.setVeterinarian(vet);
        prescription.setMedicationName(request.medicationName());
        prescription.setDosage(request.dosage());
        prescription.setFrequency(request.frequency());
        prescription.setQuantity(request.quantity());
        prescription.setInstructions(request.instructions());
        prescription.setExpirationDate(request.expirationDate());
        prescription.setMaxRefills(request.maxRefills());
        prescription.setRemainingRefills(request.maxRefills());
        prescription.setStatus(PrescriptionStatus.ACTIVE);

        Prescription saved = prescriptionRepository.save(prescription);
        logger.info("Prescription created successfully with ID: {}", saved.getId());

        return mapToPrescriptionResponse(saved);
    }

    /**
     * Find prescription by ID
     */
    @Transactional(readOnly = true)
    public PrescriptionResponse findPrescriptionById(Integer id) throws DataAccessException {
        Prescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        return mapToPrescriptionResponse(prescription);
    }

    /**
     * Find prescriptions by pet ID with optional status filter
     */
    @Transactional(readOnly = true)
    public PrescriptionListResponse findPrescriptionsByPetId(Integer petId, PrescriptionStatus status) throws DataAccessException {
        List<Prescription> prescriptions;

        if (status != null) {
            prescriptions = prescriptionRepository.findByPetIdAndStatus(petId, status);
        } else {
            prescriptions = prescriptionRepository.findByPetId(petId);
        }

        List<PrescriptionResponse> responses = prescriptions.stream()
            .map(this::mapToPrescriptionResponse)
            .collect(Collectors.toList());

        return new PrescriptionListResponse(responses, responses.size());
    }

    /**
     * Update prescription
     */
    public PrescriptionResponse updatePrescription(Integer id, PrescriptionUpdateRequest request) throws DataAccessException {
        Prescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));

        prescription.setMedicationName(request.medicationName());
        prescription.setDosage(request.dosage());
        prescription.setFrequency(request.frequency());
        prescription.setQuantity(request.quantity());
        prescription.setInstructions(request.instructions());
        prescription.setStatus(request.status());

        Prescription updated = prescriptionRepository.save(prescription);
        logger.info("Prescription updated: ID {}", id);

        return mapToPrescriptionResponse(updated);
    }

    /**
     * Request prescription refill with workflow routing
     */
    public PrescriptionRefillResponse requestRefill(Integer prescriptionId, PrescriptionRefillRequest request) throws DataAccessException {
        logger.info("Requesting refill for prescription ID: {}", prescriptionId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));

        // Business rule validation
        if (!prescription.canBeRefilled()) {
            String reason = buildRefillDenialReason(prescription);
            logger.warn("Refill request denied for prescription {}: {}", prescriptionId, reason);
            throw new IllegalStateException(reason);
        }

        // Create refill request
        PrescriptionRefill refill = new PrescriptionRefill();
        refill.setPrescription(prescription);
        refill.setRequestedDate(Instant.now());
        refill.setStatus(RefillStatus.PENDING);
        refill.setNotes(request.notes());

        PrescriptionRefill saved = refillRepository.save(refill);
        logger.info("Refill request created: ID {}", saved.getId());

        return mapToRefillResponse(saved);
    }

    /**
     * Approve refill with workflow integration
     */
    public PrescriptionRefillResponse approveRefill(Integer refillId, RefillApprovalRequest request) throws DataAccessException {
        logger.info("Approving refill ID: {}", refillId);

        PrescriptionRefill refill = refillRepository.findById(refillId)
            .orElseThrow(() -> new ResourceNotFoundException("Refill", "id", refillId));

        if (refill.getStatus() != RefillStatus.PENDING) {
            throw new IllegalStateException("Only pending refills can be approved. Current status: " + refill.getStatus());
        }

        Vet vet = vetRepository.findById(request.veterinarianId());
        if (vet == null) {
            throw new ResourceNotFoundException("Veterinarian", "id", request.veterinarianId());
        }

        // Approve refill using domain logic
        refill.approve(vet);
        if (request.notes() != null) {
            refill.setNotes(request.notes());
        }

        // Decrement prescription refills
        Prescription prescription = refill.getPrescription();
        prescription.decrementRefills();
        prescriptionRepository.save(prescription);

        PrescriptionRefill saved = refillRepository.save(refill);
        logger.info("Refill approved: ID {}", refillId);

        return mapToRefillResponse(saved);
    }

    /**
     * Reject refill with workflow integration
     */
    public PrescriptionRefillResponse rejectRefill(Integer refillId, RefillRejectionRequest request) throws DataAccessException {
        logger.info("Rejecting refill ID: {}", refillId);

        PrescriptionRefill refill = refillRepository.findById(refillId)
            .orElseThrow(() -> new ResourceNotFoundException("Refill", "id", refillId));

        if (refill.getStatus() != RefillStatus.PENDING) {
            throw new IllegalStateException("Only pending refills can be rejected. Current status: " + refill.getStatus());
        }

        Vet vet = vetRepository.findById(request.veterinarianId());
        if (vet == null) {
            throw new ResourceNotFoundException("Veterinarian", "id", request.veterinarianId());
        }

        // Reject refill using domain logic
        refill.reject(vet, request.rejectionReason());

        PrescriptionRefill saved = refillRepository.save(refill);
        logger.info("Refill rejected: ID {}", refillId);

        return mapToRefillResponse(saved);
    }

    /**
     * Find refill by ID
     */
    @Transactional(readOnly = true)
    public PrescriptionRefillResponse findRefillById(Integer id) throws DataAccessException {
        PrescriptionRefill refill = refillRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Refill", "id", id));
        return mapToRefillResponse(refill);
    }

    /**
     * Find refills by prescription ID with optional status filter
     */
    @Transactional(readOnly = true)
    public RefillListResponse findRefillsByPrescriptionId(Integer prescriptionId, RefillStatus status) throws DataAccessException {
        List<PrescriptionRefill> refills;

        if (status != null) {
            refills = refillRepository.findByPrescriptionIdAndStatus(prescriptionId, status);
        } else {
            refills = refillRepository.findByPrescriptionId(prescriptionId);
        }

        List<PrescriptionRefillResponse> responses = refills.stream()
            .map(this::mapToRefillResponse)
            .collect(Collectors.toList());

        return new RefillListResponse(responses, responses.size());
    }

    // Private helper methods

    private String buildRefillDenialReason(Prescription prescription) {
        StringBuilder reason = new StringBuilder("Prescription cannot be refilled. ");
        if (prescription.getStatus() != PrescriptionStatus.ACTIVE) {
            reason.append("Status: ").append(prescription.getStatus()).append(". ");
        }
        if (prescription.getRemainingRefills() <= 0) {
            reason.append("No refills remaining. ");
        }
        if (prescription.isExpired()) {
            reason.append("Prescription expired. ");
        }
        return reason.toString();
    }

    private PrescriptionResponse mapToPrescriptionResponse(Prescription prescription) {
        return new PrescriptionResponse(
            prescription.getId(),
            prescription.getVisit().getId(),
            prescription.getPet().getId(),
            prescription.getPet().getName(),
            prescription.getVeterinarian().getId(),
            prescription.getVeterinarian().getFirstName() + " " + prescription.getVeterinarian().getLastName(),
            prescription.getMedicationName(),
            prescription.getDosage(),
            prescription.getFrequency(),
            prescription.getQuantity(),
            prescription.getInstructions(),
            prescription.getPrescribedDate(),
            prescription.getExpirationDate(),
            prescription.getMaxRefills(),
            prescription.getRemainingRefills(),
            prescription.getStatus(),
            prescription.getCreatedAt(),
            prescription.getUpdatedAt()
        );
    }

    private PrescriptionRefillResponse mapToRefillResponse(PrescriptionRefill refill) {
        Vet approvedBy = refill.getApprovedBy();
        String vetName = approvedBy != null ? approvedBy.getFirstName() + " " + approvedBy.getLastName() : null;

        return new PrescriptionRefillResponse(
            refill.getId(),
            refill.getPrescription().getId(),
            refill.getPrescription().getMedicationName(),
            refill.getRequestedDate(),
            refill.getApprovedDate(),
            refill.getDispensedDate(),
            approvedBy != null ? approvedBy.getId() : null,
            vetName,
            refill.getStatus(),
            refill.getNotes(),
            refill.getRejectionReason(),
            refill.getCreatedAt(),
            refill.getUpdatedAt()
        );
    }
}
