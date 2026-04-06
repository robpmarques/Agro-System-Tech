package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.Sensor;

import java.util.Optional;
import java.util.UUID;

public interface SensorPort {
    /**
     * Busca um sensor pelo ID.
     *
     * @param sensorId ID do sensor
     * @return sensor, se existir
     */
    Optional<Sensor> findById(UUID sensorId);
}

