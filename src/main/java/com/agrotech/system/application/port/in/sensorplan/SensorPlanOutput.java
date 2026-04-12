package com.agrotech.system.application.port.in.sensorplan;

import com.agrotech.system.domain.model.SensorPlanStatus;

import java.time.Instant;
import java.util.UUID;

public record SensorPlanOutput(
        UUID id,
        UUID areaId,
        UUID requestedBy,
        UUID specialistId,
        SensorPlanStatus status,
        String notes,
        Instant createdAt,
        Instant reviewedAt
) {
}

