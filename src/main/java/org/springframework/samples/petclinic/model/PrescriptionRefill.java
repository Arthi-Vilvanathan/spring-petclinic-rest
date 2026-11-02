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
package org.springframework.samples.petclinic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Domain object representing a prescription refill request.
 * Follows Clean Architecture and DDD principles.
 *
 * @author PetClinic Domain Advisor
 */
@Entity
@Table(name = "prescription_refills", indexes = {
    @Index(name = "idx_refill_prescription", columnList = "prescription_id"),
    @Index(name = "idx_refill_status", columnList = "status"),
    @Index(name = "idx_refill_requested_date", columnList = "requested_date")
})
public class PrescriptionRefill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    @NotNull(message = "Prescription is required")
    private Prescription prescription;

    @NotNull(message = "Requested date is required")
    @Column(name = "requested_date", nullable = false)
    private Instant requestedDate;

    @Column(name = "approved_date")
    private Instant approvedDate;

    @Column(name = "dispensed_date")
    private Instant dispensedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_vet_id")
    private Vet approvedBy;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RefillStatus status;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (requestedDate == null) {
            requestedDate = Instant.now();
        }
        if (status == null) {
            status = RefillStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Business rule: Check if refill can be approved
     *
     * @return true if refill is in PENDING status
     */
    public boolean canBeApproved() {
        return status == RefillStatus.PENDING;
    }

    /**
     * Business rule: Check if refill can be rejected
     *
     * @return true if refill is in PENDING status
     */
    public boolean canBeRejected() {
        return status == RefillStatus.PENDING;
    }

    /**
     * Business rule: Approve the refill
     *
     * @param vet the veterinarian approving the refill
     */
    public void approve(Vet vet) {
        if (!canBeApproved()) {
            throw new IllegalStateException("Only pending refills can be approved. Current status: " + status);
        }
        this.status = RefillStatus.APPROVED;
        this.approvedDate = Instant.now();
        this.approvedBy = vet;
    }

    /**
     * Business rule: Reject the refill
     *
     * @param vet    the veterinarian rejecting the refill
     * @param reason the reason for rejection
     */
    public void reject(Vet vet, String reason) {
        if (!canBeRejected()) {
            throw new IllegalStateException("Only pending refills can be rejected. Current status: " + status);
        }
        this.status = RefillStatus.REJECTED;
        this.approvedDate = Instant.now();
        this.approvedBy = vet;
        this.rejectionReason = reason;
    }

    /**
     * Business rule: Mark refill as dispensed
     */
    public void markAsDispensed() {
        if (status != RefillStatus.APPROVED) {
            throw new IllegalStateException("Only approved refills can be dispensed. Current status: " + status);
        }
        this.status = RefillStatus.DISPENSED;
        this.dispensedDate = Instant.now();
    }

    // Getters and Setters

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public Instant getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(Instant requestedDate) {
        this.requestedDate = requestedDate;
    }

    public Instant getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Instant approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Instant getDispensedDate() {
        return dispensedDate;
    }

    public void setDispensedDate(Instant dispensedDate) {
        this.dispensedDate = dispensedDate;
    }

    public Vet getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Vet approvedBy) {
        this.approvedBy = approvedBy;
    }

    public RefillStatus getStatus() {
        return status;
    }

    public void setStatus(RefillStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
