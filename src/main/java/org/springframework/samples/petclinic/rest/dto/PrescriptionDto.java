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
import jakarta.validation.constraints.*;
import org.springframework.samples.petclinic.model.PrescriptionStatus;

import java.time.Instant;

/**
 * DTO for Prescription create request.
 * Implemented as Java Record following Java 21 best practices.
 *
 * @author PetClinic Domain Advisor
 */
@Schema(description = "Request to create a new prescription")
public record PrescriptionCreateRequest(
    @NotNull(message = "Visit ID is required")
    @Schema(description = "Visit ID", example = "1", required = true)
    Integer visitId,

    @NotNull(message = "Pet ID is required")
    @Schema(description = "Pet ID", example = "1", required = true)
    Integer petId,

    @NotNull(message = "Veterinarian ID is required")
    @Schema(description = "Veterinarian ID", example = "1", required = true)
    Integer veterinarianId,

    @NotEmpty(message = "Medication name is required")
    @Size(min = 2, max = 200, message = "Medication name must be between 2 and 200 characters")
    @Schema(description = "Medication name", example = "Amoxicillin", required = true)
    String medicationName,

    @NotEmpty(message = "Dosage is required")
    @Size(min = 2, max = 100, message = "Dosage must be between 2 and 100 characters")
    @Schema(description = "Medication dosage", example = "500mg", required = true)
    String dosage,

    @NotEmpty(message = "Frequency is required")
    @Size(min = 2, max = 100, message = "Frequency must be between 2 and 100 characters")
    @Schema(description = "Frequency of administration", example = "Twice daily", required = true)
    String frequency,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity to dispense", example = "30", required = true)
    Integer quantity,

    @Size(max = 1000, message = "Instructions cannot exceed 1000 characters")
    @Schema(description = "Special instructions", example = "Take with food")
    String instructions,

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Prescription expiration date", example = "2025-12-31T23:59:59Z", required = true)
    Instant expirationDate,

    @NotNull(message = "Maximum refills is required")
    @Min(value = 0, message = "Maximum refills must be at least 0")
    @Max(value = 12, message = "Maximum refills cannot exceed 12")
    @Schema(description = "Maximum number of refills allowed", example = "3", required = true)
    Integer maxRefills
) {
}

/**
 * DTO for Prescription response.
 * Implemented as Java Record following Java 21 best practices.
 *
 * @author PetClinic Domain Advisor
 */
@Schema(description = "Prescription response")
record PrescriptionResponse(
    @Schema(description = "Prescription ID", example = "1")
    Integer id,

    @Schema(description = "Visit ID", example = "1")
    Integer visitId,

    @Schema(description = "Pet ID", example = "1")
    Integer petId,

    @Schema(description = "Pet name", example = "Max")
    String petName,

    @Schema(description = "Veterinarian ID", example = "1")
    Integer veterinarianId,

    @Schema(description = "Veterinarian name", example = "Dr. Smith")
    String veterinarianName,

    @Schema(description = "Medication name", example = "Amoxicillin")
    String medicationName,

    @Schema(description = "Dosage", example = "500mg")
    String dosage,

    @Schema(description = "Frequency", example = "Twice daily")
    String frequency,

    @Schema(description = "Quantity", example = "30")
    Integer quantity,

    @Schema(description = "Instructions", example = "Take with food")
    String instructions,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Date prescribed", example = "2024-01-15T10:30:00Z")
    Instant prescribedDate,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Expiration date", example = "2025-12-31T23:59:59Z")
    Instant expirationDate,

    @Schema(description = "Maximum refills", example = "3")
    Integer maxRefills,

    @Schema(description = "Remaining refills", example = "2")
    Integer remainingRefills,

    @Schema(description = "Prescription status", example = "ACTIVE")
    PrescriptionStatus status,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Created timestamp", example = "2024-01-15T10:30:00Z")
    Instant createdAt,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Updated timestamp", example = "2024-01-15T10:30:00Z")
    Instant updatedAt
) {
}

/**
 * DTO for Prescription update request.
 *
 * @author PetClinic Domain Advisor
 */
@Schema(description = "Request to update prescription")
record PrescriptionUpdateRequest(
    @NotEmpty(message = "Medication name is required")
    @Size(min = 2, max = 200)
    @Schema(description = "Medication name", example = "Amoxicillin", required = true)
    String medicationName,

    @NotEmpty(message = "Dosage is required")
    @Size(min = 2, max = 100)
    @Schema(description = "Dosage", example = "500mg", required = true)
    String dosage,

    @NotEmpty(message = "Frequency is required")
    @Size(min = 2, max = 100)
    @Schema(description = "Frequency", example = "Twice daily", required = true)
    String frequency,

    @NotNull(message = "Quantity is required")
    @Min(value = 1)
    @Schema(description = "Quantity", example = "30", required = true)
    Integer quantity,

    @Size(max = 1000)
    @Schema(description = "Instructions", example = "Take with food")
    String instructions,

    @NotNull(message = "Status is required")
    @Schema(description = "Prescription status", example = "ACTIVE", required = true)
    PrescriptionStatus status
) {
}

/**
 * DTO for Prescription list response.
 *
 * @author PetClinic Domain Advisor
 */
@Schema(description = "List of prescriptions")
record PrescriptionListResponse(
    @Schema(description = "List of prescriptions")
    java.util.List<PrescriptionResponse> prescriptions,

    @Schema(description = "Total count", example = "5")
    int totalCount
) {
}
