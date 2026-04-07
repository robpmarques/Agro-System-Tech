package com.agrotech.system.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Area {

    private final UUID id;
    private String name;
    private String location;
    private double size;
    private final UUID userId;
    private final Instant createdAt;

    private Area(UUID id, String name, String location, double size, UUID userId, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Area id is required");
        this.userId = Objects.requireNonNull(userId, "Area owner is required");
        this.createdAt = Objects.requireNonNull(createdAt, "Area createdAt is required");
        this.name = normalizeName(name);
        this.location = normalizeLocation(location);
        this.size = validateSize(size);
    }

    public static Area create(String name, String location, double size, UUID userId) {
        return new Area(UUID.randomUUID(), name, location, size, userId, Instant.now());
    }

    public static Area rehydrate(UUID id, String name, String location, double size, UUID userId, Instant createdAt) {
        return new Area(id, name, location, size, userId, createdAt);
    }

    public void updateDetails(String name, String location, double size) {
        this.name = normalizeName(name);
        this.location = normalizeLocation(location);
        this.size = validateSize(size);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getSize() {
        return size;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private static String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Area name is required");
        }
        String normalized = value.trim();
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Area name must have at most 255 characters");
        }
        return normalized;
    }

    private static String normalizeLocation(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Area location is required");
        }
        String normalized = value.trim();
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Area location must have at most 255 characters");
        }
        return normalized;
    }

    private static double validateSize(double value) {
        if (value <= 0D) {
            throw new IllegalArgumentException("Area size must be greater than zero");
        }
        return value;
    }
}

