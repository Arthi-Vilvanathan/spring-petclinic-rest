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
package org.springframework.samples.petclinic.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.samples.petclinic.model.RefillStatus;

import java.time.Instant;

/**
 * DTO for prescription refill request.
 * Implemented as Java Record following Java 21 best practices.
 *
 * @author PetClinic Domain Advisor
 */
@Schema(description = "Request to refill a prescription")
public record PrescriptionRefillRequest(
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Additional notes for the refill request", example = "Running low on medication")
    String notes
) {
}

/**
 * DTO for prescription refill response.
 *
 * @author PetClinic Domain Advisor
 */
@Schema(description = "Prescription refill response")
record PrescriptionRefillResponse(
    @Schema(description = "Refill ID", example = "1")
    Integer id,

    @Schema(description = "Prescription ID", example = "1")
    Integer prescriptionId,

    @Schema(description = "Medication name", example = "Amoxicillin")
    String medicationName,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Request date", example = "2024-01-15T10:30:00Z")
    Instant requestedDate,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Approval date", example = "2024-01-16T14:20:00Z")
    Instant approvedDate,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Dispensed date", example = "2024-01-17T09:15:00Z")
    Instant dispensedDate,

    @Schema(description = "Approving veterinarian ID", example = "1")
    Integer approvedByVetId,

    @Schema(description = "Approving veterinarian name", example = "Dr. Smith")
    String approvedByVetName,

    @Schema(description = "Refill status", example = "APPROVED")
    RefillStatus status,

    @Schema(description = "Notes", example = "Running low on medication")
    String notes,

    @Schema(description = "Rejection reason", example = "Prescription expired")
    String rejectionReason,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Created timestamp", example = "2024-01-15T10:30:00Z")
    Instant createdAt,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Updated timestamp", example = "2024-01-16T14:20:00Z")
    Instant updatedAt
) {
}

/**
 * DTO for refill approval request.
 *
 * @author PetClinic Domain Advisor
 */
@Schema(description = "Request to approve a refill")
record RefillApprovalRequest(
    @NotNull(message = "Veterinarian ID is required")
    @Schema(description = "Veterinarian ID", example = "1", required = true)
    Integer veterinarianId,

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Approval notes", example = "Approved for refill")
    String notes
) {
}

/**
 * DTO for refill rejection request.
 *
 * @author PetClinic Domain Advisor
 */
@Schema(description = "Request to reject a refill")
record RefillRejectionRequest(
    @NotNull(message = "Veterinarian ID is required")
    @Schema(description = "Veterinarian ID", example = "1", required = true)
    Integer veterinarianId,

    @NotEmpty(message = "Rejection reason is required")
    @Size(min = 5, max = 500, message = "Rejection reason must be between 5 and 500 characters")
    @Schema(description = "Reason for rejection", example = "Prescription has expired", required = true)
    String rejectionReason
) {
}

/**
 * DTO for refill list response.
 *
 * @author PetClinic Domain Advisor
 */
@Schema(description = "List of prescription refills")
record RefillListResponse(
    @Schema(description = "List of refills")
    java.util.List<PrescriptionRefillResponse> refills,

    @Schema(description = "Total count", example = "3")
    int totalCount
) {
}
