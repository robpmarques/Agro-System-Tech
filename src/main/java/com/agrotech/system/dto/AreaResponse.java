package com.agrotech.system.dto;

import java.time.Instant;
import java.util.UUID;

public record AreaResponse(
        UUID id,
        String name,
        String location,
        double size,
        UUID userId,
        Instant createdAt
) {
}

