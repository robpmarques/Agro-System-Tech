package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.SensorReading;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorReadingPort {
    /**
     * Salva uma nova leitura de sensor no banco de dados.
     *
     * @param sensorReading leitura a ser salva
     * @return leitura salva com ID gerado
     */
    SensorReading save(SensorReading sensorReading);

    Optional<SensorReading> findLatestBySensorId(UUID sensorId);

    List<SensorReading> findBySensorId(UUID sensorId);

    List<SensorReading> findBySensorIdAndRecordedAtBetween(UUID sensorId, Instant startDate, Instant endDate);
}
