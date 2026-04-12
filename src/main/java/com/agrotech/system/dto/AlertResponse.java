package com.agrotech.system.dto;

import com.agrotech.system.domain.model.AlertStatus;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        UUID sensorId,
        UUID ruleId,
        Double value,
        String message,
        AlertStatus status,
        Instant triggeredAt,
        Instant resolvedAt
) {
}

