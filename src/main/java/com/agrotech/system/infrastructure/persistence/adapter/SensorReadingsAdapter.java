package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.SensorReadingPort;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.domain.model.SensorReading;
import com.agrotech.system.infrastructure.persistence.entity.SensorEntity;
import com.agrotech.system.infrastructure.persistence.entity.SensorReadings;
import com.agrotech.system.infrastructure.persistence.repo.SensorReadingsRepository;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingsAdapter implements SensorReadingPort {
    private final SensorReadingsRepository sensorReadingsRepository;

    public SensorReadingsAdapter(SensorReadingsRepository sensorReadingsRepository) {
        this.sensorReadingsRepository = sensorReadingsRepository;
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
        SensorEntity sensor = new SensorEntity();
        sensor.setId(domain.getSensorId());

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
