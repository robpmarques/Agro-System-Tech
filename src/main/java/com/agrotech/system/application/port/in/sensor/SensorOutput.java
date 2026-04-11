package com.agrotech.system.application.port.in.sensor;

import java.time.Instant;
import java.util.UUID;

public record SensorOutput(
        UUID id,
        String name,
        String type,
        String position,
        UUID areaId,
        boolean isActive,
        Instant createdAt
) {
}

