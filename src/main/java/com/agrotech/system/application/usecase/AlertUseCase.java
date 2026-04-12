package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.alert.AlertOutput;
import com.agrotech.system.application.port.in.alert.ListAlertsUseCase;
import com.agrotech.system.application.port.in.alert.ResolveAlertUseCase;
import com.agrotech.system.application.port.out.AlertPort;
import com.agrotech.system.application.port.out.AlertRealtimePort;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Alert;
import com.agrotech.system.domain.model.AlertStatus;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.dto.AlertRealtimeMessage;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AlertUseCase implements ListAlertsUseCase, ResolveAlertUseCase {

    private final AlertPort alertPort;
    private final AlertRealtimePort alertRealtimePort;
    private final SensorPort sensorPort;
    private final AreaRepositoryPort areaRepositoryPort;

    public AlertUseCase(
            AlertPort alertPort,
            AlertRealtimePort alertRealtimePort,
            SensorPort sensorPort,
            AreaRepositoryPort areaRepositoryPort
    ) {
        this.alertPort = alertPort;
        this.alertRealtimePort = alertRealtimePort;
        this.sensorPort = sensorPort;
        this.areaRepositoryPort = areaRepositoryPort;
    }

    @Override
    public List<AlertOutput> list(String status, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);

        AlertStatus parsedStatus = parseStatus(status);
        List<Alert> alerts = parsedStatus == null ? alertPort.findAll() : alertPort.findAllByStatus(parsedStatus);

        return alerts.stream()
                .filter(alert -> isVisible(alert.getSensorId(), currentUserId, currentRole))
                .map(this::toOutput)
                .toList();
    }

    @Override
    public AlertOutput resolve(UUID alertId, String requestedStatus, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        if (alertId == null) {
            throw new IllegalArgumentException("Alert id is required");
        }

        AlertStatus status = parseStatus(requestedStatus);
        if (status != AlertStatus.RESOLVED) {
            throw new IllegalArgumentException("Apenas status RESOLVED e permitido");
        }

        Alert alert = alertPort.findById(alertId)
                .orElseThrow(() -> new NotFoundException("Alerta nao encontrado"));

        ensureVisibleSensor(alert.getSensorId(), currentUserId, currentRole);

        if (alert.getStatus() != AlertStatus.RESOLVED) {
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(Instant.now());
            alert = alertPort.save(alert);
            alertRealtimePort.publish(toRealtimeMessage(alert));
        }

        return toOutput(alert);
    }

    private boolean isVisible(UUID sensorId, UUID currentUserId, Role currentRole) {
        try {
            ensureVisibleSensor(sensorId, currentUserId, currentRole);
            return true;
        } catch (NotFoundException ex) {
            return false;
        }
    }

    private Sensor ensureVisibleSensor(UUID sensorId, UUID currentUserId, Role currentRole) {
        Sensor sensor = sensorPort.findById(sensorId)
                .orElseThrow(() -> new NotFoundException("Sensor nao encontrado"));
        ensureVisibleArea(sensor.getAreaId(), currentUserId, currentRole);
        return sensor;
    }

    private void ensureVisibleArea(UUID areaId, UUID currentUserId, Role currentRole) {
        if (currentRole == Role.ADMIN) {
            areaRepositoryPort.findById(areaId)
                    .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
            return;
        }

        areaRepositoryPort.findByIdAndUserId(areaId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
    }

    private AlertStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return AlertStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Status de alerta invalido");
        }
    }

    private void ensureOperatorOrAdmin(Role role) {
        if (role != Role.OPERADOR && role != Role.ADMIN) {
            throw new ForbiddenException("Perfil sem permissao para gerenciar alertas");
        }
    }

    private AlertOutput toOutput(Alert alert) {
        return new AlertOutput(
                alert.getId(),
                alert.getSensorId(),
                alert.getRuleId(),
                alert.getValue(),
                alert.getMessage(),
                alert.getStatus(),
                alert.getTriggeredAt(),
                alert.getResolvedAt()
        );
    }

    private AlertRealtimeMessage toRealtimeMessage(Alert alert) {
        return new AlertRealtimeMessage(
                alert.getId(),
                alert.getSensorId(),
                alert.getRuleId(),
                alert.getValue(),
                alert.getMessage(),
                alert.getStatus(),
                alert.getTriggeredAt(),
                alert.getResolvedAt()
        );
    }
}

