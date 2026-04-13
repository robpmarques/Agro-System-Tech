package com.agrotech.system.domain.model;

import java.time.Instant;
import java.util.UUID;

public class PlannedSensor {
    private UUID id;
    private UUID planId;
    private String name;
    private SensorType type;
    private SensorPosition position;
    private Instant createdAt;

    public PlannedSensor() {
    }

    public PlannedSensor(
            UUID id,
            UUID planId,
            String name,
            SensorType type,
            SensorPosition position,
            Instant createdAt
    ) {
        this.id = id;
        this.planId = planId;
        this.name = name;
        this.type = type;
        this.position = position;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SensorType getType() {
        return type;
    }

    public void setType(SensorType type) {
        this.type = type;
    }

    public SensorPosition getPosition() {
        return position;
    }

    public void setPosition(SensorPosition position) {
        this.position = position;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

