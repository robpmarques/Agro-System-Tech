package com.agrotech.system.dto;

import java.time.Instant;
import java.util.UUID;

public record DashboardChartPointResponse(
        UUID sensorId,
        Instant timestamp,
        double value
) {
}

