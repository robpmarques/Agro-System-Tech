package com.agrotech.system.dto;

import java.time.Instant;
import java.util.UUID;

public record RuleResponse(
        UUID id,
        String name,
        String operator,
        Double threshold,
        boolean isActive,
        UUID sensorId,
        UUID userId,
        Instant createdAt
) {
}

