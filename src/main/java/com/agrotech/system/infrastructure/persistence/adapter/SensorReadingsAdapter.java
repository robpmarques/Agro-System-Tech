package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.SensorReadingPort;
import com.agrotech.system.domain.model.SensorReading;
import com.agrotech.system.infrastructure.persistence.entity.SensorEntity;
import com.agrotech.system.infrastructure.persistence.entity.SensorReadings;
import com.agrotech.system.infrastructure.persistence.repo.SensorReadingsRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingsAdapter implements SensorReadingPort {
    private final SensorReadingsRepository sensorReadingsRepository;
    private final EntityManager entityManager;

    public SensorReadingsAdapter(SensorReadingsRepository sensorReadingsRepository, EntityManager entityManager) {
        this.sensorReadingsRepository = sensorReadingsRepository;
        this.entityManager = entityManager;
    }

    @Override
    public SensorReading save(SensorReading sensorReading) {
        SensorReadings entity = toEntity(sensorReading);
        SensorReadings saved = sensorReadingsRepository.save(entity);
        return toDomain(saved);
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

    private SensorReadings toEntity(SensorReading domain) {
        SensorEntity sensor = entityManager.getReference(SensorEntity.class, domain.getSensorId());

        SensorReadings entity = new SensorReadings();
        entity.setId(domain.getId());
        entity.setValue(domain.getValue());
        entity.setSensor(sensor);
        entity.setRecordedAt(domain.getRecordedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setData(domain.getData());
        return entity;
    }
}
