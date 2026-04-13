package com.agrotech.system.application.port.in.dashboard;

import java.time.Instant;
import java.util.UUID;

public record DashboardChartPointOutput(
        UUID sensorId,
        Instant timestamp,
        double value
) {
}

