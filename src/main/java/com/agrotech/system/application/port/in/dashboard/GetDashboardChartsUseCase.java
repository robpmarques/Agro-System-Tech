package com.agrotech.system.application.port.in.dashboard;

import com.agrotech.system.domain.model.Role;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GetDashboardChartsUseCase {
    List<DashboardChartPointOutput> getCharts(
            UUID sensorId,
            Instant startDate,
            Instant endDate,
            UUID currentUserId,
            Role currentRole
    );
}

