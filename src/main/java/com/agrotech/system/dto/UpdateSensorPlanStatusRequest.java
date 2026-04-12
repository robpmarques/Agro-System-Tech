package com.agrotech.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSensorPlanStatusRequest(
        @NotBlank @Size(max = 32) String status,
        String notes
) {
}

