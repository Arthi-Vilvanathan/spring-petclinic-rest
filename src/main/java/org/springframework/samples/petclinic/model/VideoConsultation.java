package org.springframework.samples.petclinic.model;

import java.time.Instant;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Emergency video consultation booking.
 */
@Entity
@Table(name = "video_consultations")
public class VideoConsultation extends BaseEntity {

    public enum TriageLevel {
        MILD,
        MODERATE,
        SEVERE
    }

    public enum TriagePriority {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum Status {
        CONFIRMED
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vet_id", nullable = false)
    private Vet vet;

    @NotNull
    @Column(name = "start_date_time", nullable = false)
    private OffsetDateTime startDateTime;

    @NotNull
    @Column(name = "end_date_time", nullable = false)
    private OffsetDateTime endDateTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "triage_level", length = 16, nullable = false)
    private TriageLevel triageLevel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "triage_priority", length = 16, nullable = false)
    private TriagePriority triagePriority;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private Status status = Status.CONFIRMED;

    @NotNull
    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = Status.CONFIRMED;
        }
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Vet getVet() {
        return vet;
    }

    public void setVet(Vet vet) {
        this.vet = vet;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public TriageLevel getTriageLevel() {
        return triageLevel;
    }

    public void setTriageLevel(TriageLevel triageLevel) {
        this.triageLevel = triageLevel;
    }

    public TriagePriority getTriagePriority() {
        return triagePriority;
    }

    public void setTriagePriority(TriagePriority triagePriority) {
        this.triagePriority = triagePriority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
