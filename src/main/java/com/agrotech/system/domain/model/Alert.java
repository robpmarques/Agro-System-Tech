package com.agrotech.system.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Alert {
    private UUID id;
    private UUID sensorId;
    private UUID ruleId;
    private Double value;
    private String message;
    private AlertStatus status;
    private Instant triggeredAt;
    private Instant resolvedAt;

    public Alert() {
    }

    public Alert(
            UUID id,
            UUID sensorId,
            UUID ruleId,
            Double value,
            String message,
            AlertStatus status,
            Instant triggeredAt,
            Instant resolvedAt
    ) {
        this.id = id;
        this.sensorId = sensorId;
        this.ruleId = ruleId;
        this.value = value;
        this.message = message;
        this.status = status;
        this.triggeredAt = triggeredAt;
        this.resolvedAt = resolvedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}

