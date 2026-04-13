package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.DashboardPort;
import com.agrotech.system.domain.model.AlertStatus;
import com.agrotech.system.domain.model.SensorReading;
import com.agrotech.system.infrastructure.persistence.entity.AlertEntity;
import com.agrotech.system.infrastructure.persistence.entity.SensorReadings;
import com.agrotech.system.infrastructure.persistence.repo.AlertRepository;
import com.agrotech.system.infrastructure.persistence.repo.SensorReadingsRepository;
import com.agrotech.system.infrastructure.persistence.repo.SensorRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DashboardPersistenceAdapter implements DashboardPort {

    private final SensorReadingsRepository sensorReadingsRepository;
    private final AlertRepository alertRepository;
    private final SensorRepository sensorRepository;

    public DashboardPersistenceAdapter(
            SensorReadingsRepository sensorReadingsRepository,
            AlertRepository alertRepository,
            SensorRepository sensorRepository
    ) {
        this.sensorReadingsRepository = sensorReadingsRepository;
        this.alertRepository = alertRepository;
        this.sensorRepository = sensorRepository;
    }

    @Override
    public Map<UUID, Double> findAverageReadingBySensorId(List<UUID> visibleAreaIds) {
        if (visibleAreaIds == null || visibleAreaIds.isEmpty()) {
            return Map.of();
        }

        // Busca todos os sensores nas áreas visíveis
        List<UUID> sensorIds = sensorRepository.findByArea_IdInOrderByCreatedAtDesc(visibleAreaIds).stream()
                .map(sensor -> sensor.getId())
                .toList();

        if (sensorIds.isEmpty()) {
            return Map.of();
        }

        // Calcula média por sensor
        return sensorIds.stream()
                .collect(Collectors.toMap(
                        sensorId -> sensorId,
                        sensorId -> calculateAverageBySensorId(sensorId)
                ));
    }

    @Override
    public List<SensorReading> findLatestReadings(List<UUID> visibleAreaIds, int limit) {
        if (visibleAreaIds == null || visibleAreaIds.isEmpty()) {
            return List.of();
        }

        List<UUID> sensorIds = sensorRepository.findByArea_IdInOrderByCreatedAtDesc(visibleAreaIds).stream()
                .map(sensor -> sensor.getId())
                .toList();

        if (sensorIds.isEmpty()) {
            return List.of();
        }

        return sensorReadingsRepository.findAll().stream()
                .filter(reading -> sensorIds.contains(reading.getSensor().getId()))
                .sorted((a, b) -> b.getRecordedAt().compareTo(a.getRecordedAt()))
                .limit(limit)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countActiveAlerts(List<UUID> visibleAreaIds) {
        if (visibleAreaIds == null || visibleAreaIds.isEmpty()) {
            return 0L;
        }

        List<UUID> sensorIds = sensorRepository.findByArea_IdInOrderByCreatedAtDesc(visibleAreaIds).stream()
                .map(sensor -> sensor.getId())
                .toList();

        return alertRepository.findAllByStatusOrderByTriggeredAtDesc(AlertStatus.ACTIVE).stream()
                .filter(alert -> sensorIds.contains(alert.getSensor().getId()))
                .count();
    }

    @Override
    public List<UUID> findActiveAlertIds(List<UUID> visibleAreaIds, int limit) {
        if (visibleAreaIds == null || visibleAreaIds.isEmpty()) {
            return List.of();
        }

        List<UUID> sensorIds = sensorRepository.findByArea_IdInOrderByCreatedAtDesc(visibleAreaIds).stream()
                .map(sensor -> sensor.getId())
                .toList();

        return alertRepository.findAllByStatusOrderByTriggeredAtDesc(AlertStatus.ACTIVE).stream()
                .filter(alert -> sensorIds.contains(alert.getSensor().getId()))
                .limit(limit)
                .map(AlertEntity::getId)
                .toList();
    }

    @Override
    public List<SensorReading> findSensorSeries(UUID sensorId, Instant startDate, Instant endDate, List<UUID> visibleAreaIds) {
        if (sensorId == null || visibleAreaIds == null || visibleAreaIds.isEmpty()) {
            return List.of();
        }

        // Verifica se o sensor pertence a uma área visível
        boolean isVisible = sensorRepository.findByArea_IdInOrderByCreatedAtDesc(visibleAreaIds).stream()
                .anyMatch(sensor -> sensor.getId().equals(sensorId));

        if (!isVisible) {
            return List.of();
        }

        return sensorReadingsRepository.findAllBySensor_IdAndRecordedAtBetweenOrderByRecordedAtDesc(
                sensorId,
                startDate,
                endDate
        ).stream()
                .map(this::toDomain)
                .toList();
    }

    private double calculateAverageBySensorId(UUID sensorId) {
        return sensorReadingsRepository.findAllBySensor_IdOrderByRecordedAtDesc(sensorId).stream()
                .mapToDouble(reading -> reading.getValue() == null ? 0.0 : reading.getValue())
                .average()
                .orElse(0.0);
    }

    private SensorReading toDomain(SensorReadings entity) {
        return new SensorReading(
                entity.getId(),
                entity.getSensor().getId(),
                entity.getValue(),
                entity.getRecordedAt(),
                entity.getCreatedAt(),
                entity.getData()
        );
    }
}

