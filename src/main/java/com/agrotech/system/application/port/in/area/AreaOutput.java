package com.agrotech.system.application.port.in.area;

import java.time.Instant;
import java.util.UUID;

public record AreaOutput(
        UUID id,
        String name,
        String location,
        double size,
        UUID userId,
        Instant createdAt
) {
}

