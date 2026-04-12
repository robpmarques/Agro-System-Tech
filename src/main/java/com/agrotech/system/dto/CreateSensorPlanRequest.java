package com.agrotech.system.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSensorPlanRequest(
        @NotNull UUID areaId,
        String notes
) {
}

