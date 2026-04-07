package com.agrotech.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAreaRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String location,
        @NotNull @Positive Double size
) {
}

