package com.agrotech.system.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignSpecialistRequest(
        @NotNull UUID specialistId
) {
}

