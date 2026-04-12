package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.AlertPort;
import com.agrotech.system.domain.model.Alert;
import com.agrotech.system.domain.model.AlertStatus;
import com.agrotech.system.infrastructure.persistence.entity.AlertEntity;
import com.agrotech.system.infrastructure.persistence.entity.RuleEntity;
import com.agrotech.system.infrastructure.persistence.entity.SensorEntity;
import com.agrotech.system.infrastructure.persistence.repo.AlertRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AlertPersistenceAdapter implements AlertPort {

    private final AlertRepository alertRepository;
    private final EntityManager entityManager;

    public AlertPersistenceAdapter(AlertRepository alertRepository, EntityManager entityManager) {
        this.alertRepository = alertRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Alert save(Alert alert) {
        AlertEntity saved = alertRepository.save(toEntity(alert));
        return toDomain(saved);
    }

    @Override
    public Optional<Alert> findById(UUID id) {
        return alertRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Alert> findAll() {
        return alertRepository.findAllByOrderByTriggeredAtDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Alert> findAllByStatus(AlertStatus status) {
        return alertRepository.findAllByStatusOrderByTriggeredAtDesc(status).stream().map(this::toDomain).toList();
    }

    private AlertEntity toEntity(Alert alert) {
        SensorEntity sensor = entityManager.getReference(SensorEntity.class, alert.getSensorId());
        RuleEntity rule = entityManager.getReference(RuleEntity.class, alert.getRuleId());

        AlertEntity entity = new AlertEntity();
        entity.setId(alert.getId());
        entity.setSensor(sensor);
        entity.setRule(rule);
        entity.setValue(alert.getValue());
        entity.setMessage(alert.getMessage());
        entity.setStatus(alert.getStatus());
        entity.setTriggeredAt(alert.getTriggeredAt());
        entity.setResolvedAt(alert.getResolvedAt());
        return entity;
    }

    private Alert toDomain(AlertEntity entity) {
        return new Alert(
                entity.getId(),
                entity.getSensor().getId(),
                entity.getRule().getId(),
                entity.getValue(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getTriggeredAt(),
                entity.getResolvedAt()
        );
    }
}

