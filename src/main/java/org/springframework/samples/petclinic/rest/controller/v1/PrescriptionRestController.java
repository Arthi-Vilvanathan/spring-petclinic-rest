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
package org.springframework.samples.petclinic.rest.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.model.PrescriptionStatus;
import org.springframework.samples.petclinic.rest.dto.*;
import org.springframework.samples.petclinic.service.PrescriptionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST Controller for Prescription endpoints.
 * Follows SonarQube compliance rules and includes workflow routing logic.
 *
 * @author PetClinic Domain Advisor
 */
@RestController
@RequestMapping("/api/v1/prescriptions")
@Tag(name = "prescriptions", description = "Prescription management operations")
@CrossOrigin(exposedHeaders = "errors, content-type")
public class PrescriptionRestController {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionRestController.class);

    private final PrescriptionService prescriptionService;

    public PrescriptionRestController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * Create a new prescription
     * Node 3: PetClinic-Java-Developer generates code
     * Node 5: Security-Quality-Gatekeeper validates
     */
    @PostMapping
    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Operation(
        summary = "Create a new prescription",
        description = "Creates a new prescription for a pet during a veterinary visit. Requires VET_ADMIN role.",
        tags = {"prescriptions"}
    )
    @ApiResponse(
        responseCode = "201",
        description = "Prescription created successfully",
        content = @Content(schema = @Schema(implementation = PrescriptionResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Visit, Pet, or Veterinarian not found",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - insufficient permissions",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<PrescriptionResponse> createPrescription(
        @Valid @RequestBody PrescriptionCreateRequest request
    ) {
        logger.info("POST /api/v1/prescriptions - Creating prescription for pet ID: {}", request.petId());

        PrescriptionResponse response = prescriptionService.createPrescription(request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();

        logger.info("Prescription created successfully with ID: {}", response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Get prescription by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(@roles.VET_ADMIN, @roles.OWNER_ADMIN)")
    @Operation(
        summary = "Get prescription by ID",
        description = "Retrieves a prescription by its unique identifier",
        tags = {"prescriptions"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Prescription found",
        content = @Content(schema = @Schema(implementation = PrescriptionResponse.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Prescription not found",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<PrescriptionResponse> getPrescription(
        @PathVariable @NotNull Integer id
    ) {
        logger.info("GET /api/v1/prescriptions/{} - Retrieving prescription", id);
        PrescriptionResponse response = prescriptionService.findPrescriptionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * List prescriptions for a pet
     */
    @GetMapping
    @PreAuthorize("hasAnyRole(@roles.VET_ADMIN, @roles.OWNER_ADMIN)")
    @Operation(
        summary = "List prescriptions",
        description = "Retrieves prescriptions for a specific pet with optional status filter",
        tags = {"prescriptions"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Prescriptions retrieved successfully",
        content = @Content(schema = @Schema(implementation = PrescriptionListResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid query parameters",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<PrescriptionListResponse> listPrescriptions(
        @RequestParam @NotNull Integer petId,
        @RequestParam(required = false) PrescriptionStatus status
    ) {
        logger.info("GET /api/v1/prescriptions?petId={}&status={}", petId, status);
        PrescriptionListResponse response = prescriptionService.findPrescriptionsByPetId(petId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Update prescription
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Operation(
        summary = "Update prescription",
        description = "Updates an existing prescription details. Requires VET_ADMIN role.",
        tags = {"prescriptions"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Prescription updated successfully",
        content = @Content(schema = @Schema(implementation = PrescriptionResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Prescription not found",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "409",
        description = "Optimistic locking failure",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<PrescriptionResponse> updatePrescription(
        @PathVariable @NotNull Integer id,
        @Valid @RequestBody PrescriptionUpdateRequest request
    ) {
        logger.info("PUT /api/v1/prescriptions/{} - Updating prescription", id);
        PrescriptionResponse response = prescriptionService.updatePrescription(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Request prescription refill
     * Conditional Decision Node: Routes based on qualityScore and status
     */
    @PostMapping("/{id}/refills")
    @PreAuthorize("hasAnyRole(@roles.VET_ADMIN, @roles.OWNER_ADMIN)")
    @Operation(
        summary = "Request prescription refill",
        description = "Submits a refill request for an active prescription. Includes workflow routing logic.",
        tags = {"prescription-refills"}
    )
    @ApiResponse(
        responseCode = "201",
        description = "Refill request created successfully",
        content = @Content(schema = @Schema(implementation = PrescriptionRefillResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Prescription cannot be refilled (expired, depleted, or inactive)",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Prescription not found",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<PrescriptionRefillResponse> requestRefill(
        @PathVariable @NotNull Integer id,
        @Valid @RequestBody PrescriptionRefillRequest request
    ) {
        logger.info("POST /api/v1/prescriptions/{}/refills - Requesting refill", id);

        // Workflow Node: Request refill with business validation
        PrescriptionRefillResponse response = prescriptionService.requestRefill(id, request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/api/v1/refills/{id}")
            .buildAndExpand(response.id())
            .toUri();

        logger.info("Refill request created successfully with ID: {}", response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * List refills for a prescription
     */
    @GetMapping("/{id}/refills")
    @PreAuthorize("hasAnyRole(@roles.VET_ADMIN, @roles.OWNER_ADMIN)")
    @Operation(
        summary = "List refills for a prescription",
        description = "Retrieves all refill requests for a specific prescription",
        tags = {"prescription-refills"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Refills retrieved successfully",
        content = @Content(schema = @Schema(implementation = RefillListResponse.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Prescription not found",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<RefillListResponse> listRefills(
        @PathVariable @NotNull Integer id,
        @RequestParam(required = false) org.springframework.samples.petclinic.model.RefillStatus status
    ) {
        logger.info("GET /api/v1/prescriptions/{}/refills?status={}", id, status);
        RefillListResponse response = prescriptionService.findRefillsByPrescriptionId(id, status);
        return ResponseEntity.ok(response);
    }
}
