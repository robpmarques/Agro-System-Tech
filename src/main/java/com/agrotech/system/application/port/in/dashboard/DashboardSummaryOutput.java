package com.agrotech.system.application.port.in.dashboard;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DashboardSummaryOutput(
        Map<UUID, Double> averageBySensorId,
        List<DashboardChartPointOutput> recentReadings,
        long activeAlertsTotal,
        List<UUID> activeAlertIds
) {
}

