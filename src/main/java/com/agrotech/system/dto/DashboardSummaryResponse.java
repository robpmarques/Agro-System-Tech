package com.agrotech.system.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DashboardSummaryResponse(
        Map<UUID, Double> averageBySensorId,
        List<DashboardChartPointResponse> recentReadings,
        long activeAlertsTotal,
        List<UUID> activeAlertIds
) {
}

