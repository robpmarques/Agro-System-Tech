package com.agrotech.system.application.port.in.alert;

import com.agrotech.system.domain.model.AlertStatus;

import java.time.Instant;
import java.util.UUID;

public record AlertOutput(
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

