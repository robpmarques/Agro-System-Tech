package com.agrotech.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlannedSensorRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 32) String type,
        @NotBlank @Size(max = 32) String position
) {
}

