package com.agrotech.system.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Sensor {
    private UUID id;
    private String name;
    private String type;
    private String position;
    private UUID areaId;
    private boolean active;
    private Instant createdAt;

    public Sensor() {
    }

    public Sensor(UUID id, String name, String type, String position, UUID areaId, boolean active, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.position = position;
        this.areaId = areaId;
        this.active = active;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public UUID getAreaId() {
        return areaId;
    }

    public void setAreaId(UUID areaId) {
        this.areaId = areaId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

