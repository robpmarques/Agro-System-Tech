package com.agrotech.system.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SensorReadingResponse(
        UUID readingId,
        UUID sensorId,
        Double value,
        Instant recordedAt,
        Instant createdAt,
        Map<String, Object> data
) {
}

