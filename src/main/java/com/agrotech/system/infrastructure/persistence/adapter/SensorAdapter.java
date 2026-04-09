package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.infrastructure.persistence.entity.SensorEntity;
import com.agrotech.system.infrastructure.persistence.repo.SensorRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import com.agrotech.system.infrastructure.persistence.entity.AreaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SensorAdapter implements SensorPort {

    private final SensorRepository sensorRepository;
    private final EntityManager entityManager;

    public SensorAdapter(SensorRepository sensorRepository, EntityManager entityManager) {
        this.sensorRepository = sensorRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Sensor save(Sensor sensor) {
        SensorEntity saved = sensorRepository.save(toEntity(sensor));
        return toDomain(saved);
    }

    @Override
    public List<Sensor> findAll() {
        return sensorRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Sensor> findAllByAreaIds(List<UUID> areaIds) {
        if (areaIds == null || areaIds.isEmpty()) {
            return List.of();
        }
        return sensorRepository.findByArea_IdInOrderByCreatedAtDesc(areaIds).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Sensor> findById(UUID sensorId) {
        return sensorRepository.findById(sensorId).map(this::toDomain);
    }

    @Override
    public List<Sensor> findAllActive() {
        return sensorRepository.findByActiveTrue().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID sensorId) {
        sensorRepository.deleteById(sensorId);
    }

    private Sensor toDomain(SensorEntity entity) {
        return new Sensor(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getPosition(),
                entity.getArea().getId(),
                entity.getActive(),
                entity.getCreatedAt()
        );
    }

    private SensorEntity toEntity(Sensor sensor) {
        AreaEntity area = entityManager.getReference(AreaEntity.class, sensor.getAreaId());

        SensorEntity entity = new SensorEntity();
        entity.setId(sensor.getId());
        entity.setName(sensor.getName());
        entity.setType(sensor.getType());
        entity.setPosition(sensor.getPosition());
        entity.setArea(area);
        entity.setActive(sensor.isActive());
        entity.setCreatedAt(sensor.getCreatedAt());
        return entity;
    }
}

