package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.dashboard.DashboardChartPointOutput;
import com.agrotech.system.application.port.in.dashboard.DashboardSummaryOutput;
import com.agrotech.system.application.port.in.dashboard.GetDashboardChartsUseCase;
import com.agrotech.system.application.port.in.dashboard.GetDashboardSummaryUseCase;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.DashboardPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.domain.model.SensorReading;

import java.time.Instant;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DashboardUseCase implements GetDashboardSummaryUseCase, GetDashboardChartsUseCase {

    private static final int RECENT_READINGS_LIMIT = 10;
    private static final int ACTIVE_ALERT_IDS_LIMIT = 20;

    private final DashboardPort dashboardPort;
    private final AreaRepositoryPort areaRepositoryPort;
    private final SensorPort sensorPort;

    public DashboardUseCase(
            DashboardPort dashboardPort,
            AreaRepositoryPort areaRepositoryPort,
            SensorPort sensorPort
    ) {
        this.dashboardPort = dashboardPort;
        this.areaRepositoryPort = areaRepositoryPort;
        this.sensorPort = sensorPort;
    }

    @Override
    public DashboardSummaryOutput getSummary(UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);

        List<UUID> visibleAreaIds = resolveVisibleAreaIds(currentUserId, currentRole);
        if (visibleAreaIds.isEmpty()) {
            return new DashboardSummaryOutput(Map.of(), List.of(), 0L, List.of());
        }

        Map<UUID, Double> averages = dashboardPort.findAverageReadingBySensorId(visibleAreaIds);
        List<DashboardChartPointOutput> recentReadings = dashboardPort.findLatestReadings(visibleAreaIds, RECENT_READINGS_LIMIT).stream()
                .map(reading -> new DashboardChartPointOutput(
                        reading.getSensorId(),
                        reading.getRecordedAt(),
                        reading.getValue() == null ? 0.0 : reading.getValue()
                ))
                .toList();
        long activeAlertsTotal = dashboardPort.countActiveAlerts(visibleAreaIds);
        List<UUID> activeAlertIds = dashboardPort.findActiveAlertIds(visibleAreaIds, ACTIVE_ALERT_IDS_LIMIT);

        return new DashboardSummaryOutput(averages, recentReadings, activeAlertsTotal, activeAlertIds);
    }

    @Override
    public List<DashboardChartPointOutput> getCharts(
            UUID sensorId,
            Instant startDate,
            Instant endDate,
            UUID currentUserId,
            Role currentRole
    ) {
        ensureOperatorOrAdmin(currentRole);
        if (sensorId == null) {
            throw new IllegalArgumentException("Sensor id is required");
        }
        validatePeriod(startDate, endDate);

        List<UUID> visibleAreaIds = resolveVisibleAreaIds(currentUserId, currentRole);
        if (visibleAreaIds.isEmpty()) {
            return List.of();
        }

        return dashboardPort.findSensorSeries(sensorId, startDate, endDate, visibleAreaIds).stream()
                .map(this::toChartPoint)
                .sorted(Comparator.comparing(DashboardChartPointOutput::timestamp))
                .toList();
    }

    private void validatePeriod(Instant startDate, Instant endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate e endDate sao obrigatorios");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate deve ser maior ou igual a startDate");
        }
    }

    private DashboardChartPointOutput toChartPoint(SensorReading reading) {
        return new DashboardChartPointOutput(
                reading.getSensorId(),
                reading.getRecordedAt(),
                reading.getValue() == null ? 0.0 : reading.getValue()
        );
    }

    private List<UUID> resolveVisibleAreaIds(UUID currentUserId, Role currentRole) {
        if (currentRole == Role.ADMIN) {
            return sensorPort.findAll().stream()
                    .map(Sensor::getAreaId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
        }

        List<UUID> areaIds = areaRepositoryPort.findAreaIdsByUserId(currentUserId);
        if (areaIds == null) {
            return Collections.emptyList();
        }
        return areaIds;
    }

    private void ensureOperatorOrAdmin(Role role) {
        if (role != Role.OPERADOR && role != Role.ADMIN) {
            throw new ForbiddenException("Perfil sem permissao para visualizar dashboard");
        }
    }
}

