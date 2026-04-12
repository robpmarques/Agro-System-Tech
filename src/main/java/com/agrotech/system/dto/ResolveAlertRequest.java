package com.agrotech.system.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveAlertRequest(
        @NotBlank String status
) {
}

