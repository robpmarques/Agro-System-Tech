package com.agrotech.system.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Rule {
    private UUID id;
    private String name;
    private String operator;
    private Double threshold;
    private boolean active;
    private UUID sensorId;
    private UUID userId;
    private Instant createdAt;

    public Rule() {
    }

    public Rule(UUID id, String name, String operator, Double threshold, boolean active, UUID sensorId, UUID userId, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.operator = operator;
        this.threshold = threshold;
        this.active = active;
        this.sensorId = sensorId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

