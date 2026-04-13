package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.SensorReading;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DashboardPort {

    /**
     * Retorna media de leitura por sensor dentro do escopo das areas visiveis.
     */
    Map<UUID, Double> findAverageReadingBySensorId(List<UUID> visibleAreaIds);

    /**
     * Retorna as ultimas leituras do escopo informado, limitado por quantidade.
     */
    List<SensorReading> findLatestReadings(List<UUID> visibleAreaIds, int limit);

    /**
     * Retorna total de alertas ativos no escopo informado.
     */
    long countActiveAlerts(List<UUID> visibleAreaIds);

    /**
     * Retorna IDs de alertas ativos no escopo informado, limitados por quantidade.
     */
    List<UUID> findActiveAlertIds(List<UUID> visibleAreaIds, int limit);

    /**
     * Retorna serie temporal ordenada por timestamp ascendente para um sensor no periodo.
     */
    List<SensorReading> findSensorSeries(UUID sensorId, Instant startDate, Instant endDate, List<UUID> visibleAreaIds);
}

