package com.agrotech.system.application.port.in.rule;

import java.time.Instant;
import java.util.UUID;

public record RuleOutput(
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

