package com.agrotech.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRuleRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 10) String operator,
        @NotNull Double threshold,
        Boolean isActive
) {
}

