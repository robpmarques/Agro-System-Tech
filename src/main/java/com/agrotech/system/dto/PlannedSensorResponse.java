package com.agrotech.system.dto;

import com.agrotech.system.domain.model.SensorPosition;
import com.agrotech.system.domain.model.SensorType;

import java.time.Instant;
import java.util.UUID;

public record PlannedSensorResponse(
        UUID id,
        UUID planId,
        String name,
        SensorType type,
        SensorPosition position,
        Instant createdAt
) {
}

