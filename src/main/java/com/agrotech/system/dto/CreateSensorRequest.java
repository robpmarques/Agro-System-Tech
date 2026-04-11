package com.agrotech.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSensorRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 50) String type,
        @NotBlank @Size(max = 255) String position,
        @NotNull UUID areaId,
        Boolean isActive
) {
}

