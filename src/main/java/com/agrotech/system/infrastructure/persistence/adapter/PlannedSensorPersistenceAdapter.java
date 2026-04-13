package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.PlannedSensorPort;
import com.agrotech.system.domain.model.PlannedSensor;
import com.agrotech.system.domain.model.SensorPosition;
import com.agrotech.system.domain.model.SensorType;
import com.agrotech.system.infrastructure.persistence.entity.PlannedSensorEntity;
import com.agrotech.system.infrastructure.persistence.repo.PlannedSensorRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PlannedSensorPersistenceAdapter implements PlannedSensorPort {

    private final PlannedSensorRepository plannedSensorRepository;

    public PlannedSensorPersistenceAdapter(PlannedSensorRepository plannedSensorRepository) {
        this.plannedSensorRepository = plannedSensorRepository;
    }

    @Override
    public PlannedSensor save(PlannedSensor plannedSensor) {
        PlannedSensorEntity saved = plannedSensorRepository.save(toEntity(plannedSensor));
        return toDomain(saved);
    }

    @Override
    public Optional<PlannedSensor> findById(UUID plannedSensorId) {
        return plannedSensorRepository.findById(plannedSensorId).map(this::toDomain);
    }

    @Override
    public List<PlannedSensor> findAllByPlanId(UUID planId) {
        return plannedSensorRepository.findAllByPlanIdOrderByCreatedAtDesc(planId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID plannedSensorId) {
        plannedSensorRepository.deleteById(plannedSensorId);
    }

    @Override
    public void deleteAllByPlanId(UUID planId) {
        plannedSensorRepository.deleteAllByPlanId(planId);
    }

    private PlannedSensor toDomain(PlannedSensorEntity entity) {
        return new PlannedSensor(
                entity.getId(),
                entity.getPlanId(),
                entity.getName(),
                parseType(entity.getType()),
                parsePosition(entity.getPosition()),
                entity.getCreatedAt()
        );
    }

    private PlannedSensorEntity toEntity(PlannedSensor plannedSensor) {
        PlannedSensorEntity entity = new PlannedSensorEntity();
        entity.setId(plannedSensor.getId());
        entity.setPlanId(plannedSensor.getPlanId());
        entity.setName(plannedSensor.getName());
        entity.setType(plannedSensor.getType() == null ? null : plannedSensor.getType().name());
        entity.setPosition(plannedSensor.getPosition() == null ? null : plannedSensor.getPosition().name());
        entity.setCreatedAt(plannedSensor.getCreatedAt());
        return entity;
    }

    private SensorType parseType(String value) {
        return value == null ? null : SensorType.valueOf(value);
    }

    private SensorPosition parsePosition(String value) {
        return value == null ? null : SensorPosition.valueOf(value);
    }
}

