package com.agrotech.system.application.port.in.sensorplan;

import com.agrotech.system.domain.model.SensorPosition;
import com.agrotech.system.domain.model.SensorType;

import java.time.Instant;
import java.util.UUID;

public record PlannedSensorOutput(
        UUID id,
        UUID planId,
        String name,
        SensorType type,
        SensorPosition position,
        Instant createdAt
) {
}

