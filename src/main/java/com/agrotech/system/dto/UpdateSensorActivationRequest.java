package com.agrotech.system.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateSensorActivationRequest(
        @NotNull Boolean isActive
) {
}

