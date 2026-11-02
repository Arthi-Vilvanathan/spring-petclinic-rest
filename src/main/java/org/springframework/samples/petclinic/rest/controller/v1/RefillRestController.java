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
import org.springframework.samples.petclinic.rest.dto.PrescriptionRefillResponse;
import org.springframework.samples.petclinic.rest.dto.RefillApprovalRequest;
import org.springframework.samples.petclinic.rest.dto.RefillRejectionRequest;
import org.springframework.samples.petclinic.service.PrescriptionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Prescription Refill operations.
 * Implements workflow routing with conditional decision nodes and HITL gate.
 *
 * WORKFLOW ROUTING LOGIC:
 * - Node 5: Security-Quality-Gatekeeper validates quality
 * - Conditional Router: If qualityScore < 80 OR status == "REJECTED"
 *   → Route back to Node 3 (max 2 iterations)
 * - If status == "APPROVED" → Proceed to Human-in-the-Loop Gate
 * - HITL Gate: Human approval required before Git commit
 * - Node 6: Git-DevOps-Publisher-Agent creates PR
 *
 * @author PetClinic Domain Advisor
 */
@RestController
@RequestMapping("/api/v1/refills")
@Tag(name = "prescription-refills", description = "Prescription refill approval operations with workflow integration")
@CrossOrigin(exposedHeaders = "errors, content-type")
public class RefillRestController {

    private static final Logger logger = LoggerFactory.getLogger(RefillRestController.class);
    private static final int QUALITY_THRESHOLD = 80;
    private static final int MAX_ITERATIONS = 2;

    private final PrescriptionService prescriptionService;

    public RefillRestController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * Get refill by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(@roles.VET_ADMIN, @roles.OWNER_ADMIN)")
    @Operation(
        summary = "Get refill by ID",
        description = "Retrieves a prescription refill by its unique identifier",
        tags = {"prescription-refills"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Refill found",
        content = @Content(schema = @Schema(implementation = PrescriptionRefillResponse.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Refill not found",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<PrescriptionRefillResponse> getRefill(
        @PathVariable @NotNull Integer id
    ) {
        logger.info("GET /api/v1/refills/{} - Retrieving refill", id);
        PrescriptionRefillResponse response = prescriptionService.findRefillById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Approve prescription refill
     *
     * WORKFLOW NODE: Conditional Decision Router
     * After approval, checks quality gate:
     * - If qualityScore >= 80 AND status == "APPROVED" → Proceed to HITL Gate
     * - Else → Route back to code regeneration (max 2 iterations)
     *
     * HUMAN-IN-THE-LOOP GATE:
     * This endpoint represents the approval gate where a human developer
     * must review the PR summary, code diff, and test results before
     * proceeding to Node 6 (Git-DevOps-Publisher-Agent)
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Operation(
        summary = "Approve prescription refill (HITL Gate)",
        description = """
            Approves a pending prescription refill request.
            
            WORKFLOW INTEGRATION:
            1. Validates refill status is PENDING
            2. Executes approval business logic
            3. Quality gate check: qualityScore >= 80
            4. HITL Gate: Requires human approval
            5. If approved → Triggers Git-DevOps-Publisher-Agent
            
            HITL APPROVAL CARD DISPLAYS:
            - PR summary and refill details
            - Code diff and test results
            - Quality score and compliance status
            
            BUTTONS: [Approve & Push to GitHub] / [Reject & Terminate]
            """,
        tags = {"prescription-refills"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Refill approved successfully - HITL approval required before Git commit",
        content = @Content(schema = @Schema(implementation = PrescriptionRefillResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Refill cannot be approved (not in PENDING status or quality check failed)",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Refill or Veterinarian not found",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<PrescriptionRefillResponse> approveRefill(
        @PathVariable @NotNull Integer id,
        @Valid @RequestBody RefillApprovalRequest request
    ) {
        logger.info("POST /api/v1/refills/{}/approve - Approving refill (HITL Gate)", id);

        // Execute approval business logic
        PrescriptionRefillResponse response = prescriptionService.approveRefill(id, request);

        // Simulate quality gate check (Node 5: Security-Quality-Gatekeeper)
        int qualityScore = calculateQualityScore(response);
        logger.info("Quality Score: {} (Threshold: {})", qualityScore, QUALITY_THRESHOLD);

        // Conditional Decision Node: Route based on quality
        if (qualityScore < QUALITY_THRESHOLD) {
            logger.warn("Quality score below threshold. Routing back for regeneration.");
            // In a real workflow engine, this would trigger Node 3 regeneration
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header("X-Workflow-Action", "REGENERATE")
                .header("X-Quality-Score", String.valueOf(qualityScore))
                .header("X-Feedback", "Code quality below threshold. Please review and regenerate.")
                .body(response);
        }

        // Quality gate passed - Proceed to HITL Gate
        logger.info("✓ Quality gate passed. Proceeding to HITL approval gate.");
        logger.info("HITL APPROVAL CARD:");
        logger.info("├─ PR Summary: Prescription refill #{} approved", id);
        logger.info("├─ Medication: {}", response.medicationName());
        logger.info("├─ Quality Score: {}/100", qualityScore);
        logger.info("├─ Status: APPROVED");
        logger.info("├─ Approver: {}", response.approvedByVetName());
        logger.info("└─ Awaiting human approval to push to GitHub...");

        // Response includes workflow metadata for HITL gate
        return ResponseEntity.ok()
            .header("X-Workflow-Stage", "HITL_APPROVAL_PENDING")
            .header("X-Quality-Score", String.valueOf(qualityScore))
            .header("X-Next-Action", "HUMAN_APPROVAL_REQUIRED")
            .header("X-PR-Ready", "true")
            .body(response);
    }

    /**
     * Reject prescription refill
     *
     * WORKFLOW NODE: Conditional Decision Router
     * When rejected, triggers feedback loop to Node 3 (max 2 iterations)
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Operation(
        summary = "Reject prescription refill",
        description = """
            Rejects a pending prescription refill request with reason.
            
            WORKFLOW INTEGRATION:
            1. Validates refill status is PENDING
            2. Executes rejection business logic
            3. Conditional Router: Routes feedback to Node 3
            4. Regeneration loop: max 2 iterations
            5. If max iterations reached → Terminate workflow
            """,
        tags = {"prescription-refills"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Refill rejected successfully - feedback routed for regeneration",
        content = @Content(schema = @Schema(implementation = PrescriptionRefillResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Refill cannot be rejected (not in PENDING status)",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Refill or Veterinarian not found",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<PrescriptionRefillResponse> rejectRefill(
        @PathVariable @NotNull Integer id,
        @Valid @RequestBody RefillRejectionRequest request
    ) {
        logger.info("POST /api/v1/refills/{}/reject - Rejecting refill", id);

        PrescriptionRefillResponse response = prescriptionService.rejectRefill(id, request);

        // Conditional Router: Route feedback for regeneration
        logger.warn("Refill rejected. Routing feedback to Node 3 for regeneration.");
        logger.info("Rejection reason: {}", request.rejectionReason());

        return ResponseEntity.ok()
            .header("X-Workflow-Action", "REJECTED")
            .header("X-Feedback", request.rejectionReason())
            .header("X-Route-To", "NODE_3_REGENERATION")
            .body(response);
    }

    /**
     * Human approval endpoint - simulates HITL gate approval
     *
     * This endpoint simulates the [Approve & Push to GitHub] button
     * in the HITL approval card. When called, it triggers Node 6
     * (Git-DevOps-Publisher-Agent) to create the PR.
     */
    @PostMapping("/{id}/human-approve")
    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Operation(
        summary = "Human approval gate - Push to GitHub",
        description = """
            HUMAN-IN-THE-LOOP APPROVAL GATE
            
            This endpoint represents the [Approve & Push to GitHub] action
            from the HITL approval card.
            
            WORKFLOW:
            1. Developer reviews PR summary, code diff, and test results
            2. Developer clicks [Approve & Push to GitHub]
            3. Triggers Node 6: Git-DevOps-Publisher-Agent
            4. Creates branch: feature/JIRA-402-prescription-refills
            5. Commits all files with proper messages
            6. Creates Pull Request on GitHub
            
            This is the final gate before code reaches the repository.
            """,
        tags = {"prescription-refills", "workflow"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Human approval granted - triggering Git-DevOps-Publisher-Agent",
        content = @Content(schema = @Schema(implementation = PrescriptionRefillResponse.class))
    )
    public ResponseEntity<PrescriptionRefillResponse> humanApprove(
        @PathVariable @NotNull Integer id
    ) {
        logger.info("POST /api/v1/refills/{}/human-approve - HITL approval granted", id);

        PrescriptionRefillResponse response = prescriptionService.findRefillById(id);

        // Trigger Node 6: Git-DevOps-Publisher-Agent
        logger.info("✓ HITL approval granted!");
        logger.info("Triggering Node 6: Git-DevOps-Publisher-Agent");
        logger.info("├─ Creating branch: feature/JIRA-402-prescription-refills");
        logger.info("├─ Committing files...");
        logger.info("├─ Creating Pull Request...");
        logger.info("└─ PR URL: https://github.com/org/petclinic/pull/XXX");

        return ResponseEntity.ok()
            .header("X-Workflow-Stage", "GIT_COMMIT_IN_PROGRESS")
            .header("X-Branch", "feature/JIRA-402-prescription-refills")
            .header("X-PR-Status", "CREATING")
            .body(response);
    }

    /**
     * Calculate quality score for workflow routing decision
     * In production, this would integrate with SonarQube or similar tools
     */
    private int calculateQualityScore(PrescriptionRefillResponse response) {
        // Simulated quality score calculation
        int score = 100;

        // Check if all required fields are present
        if (response.approvedByVetName() == null) score -= 20;
        if (response.notes() == null || response.notes().isEmpty()) score -= 10;

        // Simulate code quality checks
        // In production: integrate with SonarQube, code coverage, etc.
        return score;
    }
}
