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
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain object representing a veterinary prescription for a pet.
 * Follows Clean Architecture and DDD principles.
 *
 * @author PetClinic Domain Advisor
 */
@Entity
@Table(name = "prescriptions", indexes = {
    @Index(name = "idx_prescription_visit", columnList = "visit_id"),
    @Index(name = "idx_prescription_pet", columnList = "pet_id"),
    @Index(name = "idx_prescription_vet", columnList = "vet_id"),
    @Index(name = "idx_prescription_status", columnList = "status")
})
public class Prescription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    @NotNull(message = "Visit is required")
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @NotNull(message = "Pet is required")
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    @NotNull(message = "Veterinarian is required")
    private Vet veterinarian;

    @NotEmpty(message = "Medication name is required")
    @Size(min = 2, max = 200, message = "Medication name must be between 2 and 200 characters")
    @Column(name = "medication_name", nullable = false, length = 200)
    private String medicationName;

    @NotEmpty(message = "Dosage is required")
    @Size(min = 2, max = 100, message = "Dosage must be between 2 and 100 characters")
    @Column(name = "dosage", nullable = false, length = 100)
    private String dosage;

    @NotEmpty(message = "Frequency is required")
    @Size(min = 2, max = 100, message = "Frequency must be between 2 and 100 characters")
    @Column(name = "frequency", nullable = false, length = 100)
    private String frequency;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Size(max = 1000, message = "Instructions cannot exceed 1000 characters")
    @Column(name = "instructions", length = 1000)
    private String instructions;

    @NotNull(message = "Prescribed date is required")
    @Column(name = "prescribed_date", nullable = false)
    private Instant prescribedDate;

    @NotNull(message = "Expiration date is required")
    @Column(name = "expiration_date", nullable = false)
    private Instant expirationDate;

    @NotNull(message = "Maximum refills is required")
    @Min(value = 0, message = "Maximum refills must be at least 0")
    @Max(value = 12, message = "Maximum refills cannot exceed 12")
    @Column(name = "max_refills", nullable = false)
    private Integer maxRefills;

    @NotNull(message = "Remaining refills is required")
    @Min(value = 0, message = "Remaining refills must be at least 0")
    @Column(name = "remaining_refills", nullable = false)
    private Integer remainingRefills;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PrescriptionStatus status;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionRefill> refills = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (prescribedDate == null) {
            prescribedDate = Instant.now();
        }
        if (status == null) {
            status = PrescriptionStatus.ACTIVE;
        }
        if (remainingRefills == null && maxRefills != null) {
            remainingRefills = maxRefills;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Business rule: Check if prescription can be refilled
     *
     * @return true if prescription can be refilled
     */
    public boolean canBeRefilled() {
        return status == PrescriptionStatus.ACTIVE
            && remainingRefills > 0
            && Instant.now().isBefore(expirationDate);
    }

    /**
     * Business rule: Decrement remaining refills
     * Updates status to DEPLETED if no refills remain
     */
    public void decrementRefills() {
        if (remainingRefills > 0) {
            remainingRefills--;
            if (remainingRefills == 0) {
                status = PrescriptionStatus.DEPLETED;
            }
        }
    }

    /**
     * Business rule: Check if prescription is expired
     *
     * @return true if prescription has expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expirationDate);
    }

    /**
     * Business rule: Mark prescription as expired
     */
    public void markAsExpired() {
        if (isExpired()) {
            this.status = PrescriptionStatus.EXPIRED;
        }
    }

    // Getters and Setters

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public Vet getVeterinarian() {
        return veterinarian;
    }

    public void setVeterinarian(Vet veterinarian) {
        this.veterinarian = veterinarian;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Instant getPrescribedDate() {
        return prescribedDate;
    }

    public void setPrescribedDate(Instant prescribedDate) {
        this.prescribedDate = prescribedDate;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getMaxRefills() {
        return maxRefills;
    }

    public void setMaxRefills(Integer maxRefills) {
        this.maxRefills = maxRefills;
    }

    public Integer getRemainingRefills() {
        return remainingRefills;
    }

    public void setRemainingRefills(Integer remainingRefills) {
        this.remainingRefills = remainingRefills;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    public List<PrescriptionRefill> getRefills() {
        return refills;
    }

    public void setRefills(List<PrescriptionRefill> refills) {
        this.refills = refills;
    }

    public void addRefill(PrescriptionRefill refill) {
        refills.add(refill);
        refill.setPrescription(this);
    }

    public void removeRefill(PrescriptionRefill refill) {
        refills.remove(refill);
        refill.setPrescription(null);
    }
}
