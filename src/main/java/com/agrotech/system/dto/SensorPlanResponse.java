package com.agrotech.system.dto;

import com.agrotech.system.domain.model.SensorPlanStatus;

import java.time.Instant;
import java.util.UUID;

public record SensorPlanResponse(
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

