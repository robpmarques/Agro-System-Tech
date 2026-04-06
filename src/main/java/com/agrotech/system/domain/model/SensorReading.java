package com.agrotech.system.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class SensorReading {
    private UUID id;
    private UUID sensorId;
    private Double value;
    private Instant recordedAt;
    private Instant createdAt;
    private Map<String, Object> data;

    public SensorReading() {
    }

    public SensorReading(UUID id, UUID sensorId, Double value, Instant recordedAt, Instant createdAt, Map<String, Object> data) {
        this.id = id;
        this.sensorId = sensorId;
        this.value = value;
        this.recordedAt = recordedAt;
        this.createdAt = createdAt;
        this.data = data;
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

