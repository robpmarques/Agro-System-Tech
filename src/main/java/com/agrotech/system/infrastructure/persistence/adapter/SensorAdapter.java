package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.infrastructure.persistence.entity.SensorEntity;
import com.agrotech.system.infrastructure.persistence.repo.SensorRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SensorAdapter implements SensorPort {

    private final SensorRepository sensorRepository;

    public SensorAdapter(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    @Override
    public Optional<Sensor> findById(UUID sensorId) {
        return sensorRepository.findById(sensorId).map(this::toDomain);
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
}

