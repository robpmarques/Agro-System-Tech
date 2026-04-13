package com.agrotech.system.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SensorPlan {
    private UUID id;
    private UUID areaId;
    private UUID requestedBy;
    private UUID specialistId;
    private SensorPlanStatus status;
    private String notes;
    private Instant createdAt;
    private Instant reviewedAt;

    public SensorPlan() {
    }

    public SensorPlan(
            UUID id,
            UUID areaId,
            UUID requestedBy,
            UUID specialistId,
            SensorPlanStatus status,
            String notes,
            Instant createdAt,
            Instant reviewedAt
    ) {
        this.id = id;
        this.areaId = areaId;
        this.requestedBy = requestedBy;
        this.specialistId = specialistId;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAreaId() {
        return areaId;
    }

    public void setAreaId(UUID areaId) {
        this.areaId = areaId;
    }

    public UUID getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(UUID requestedBy) {
        this.requestedBy = requestedBy;
    }

    public UUID getSpecialistId() {
        return specialistId;
    }

    public void setSpecialistId(UUID specialistId) {
        this.specialistId = specialistId;
    }

    public SensorPlanStatus getStatus() {
        return status;
    }

    public void setStatus(SensorPlanStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}

