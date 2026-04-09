package com.agrotech.system.dto;

import java.time.Instant;
import java.util.UUID;

public record SensorResponse(
        UUID id,
        String name,
        String type,
        String position,
        UUID areaId,
        Boolean isActive,
        Instant createdAt
) {
}

