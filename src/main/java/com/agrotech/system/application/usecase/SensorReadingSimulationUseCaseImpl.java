package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.SensorReadingSimulationUseCase;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.application.port.out.SensorReadingPort;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.domain.model.SensorReading;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class SensorReadingSimulationUseCaseImpl implements SensorReadingSimulationUseCase {

    private final SensorPort sensorPort;
    private final SensorReadingPort sensorReadingPort;
    private final AlertEvaluationService alertEvaluationService;

    public SensorReadingSimulationUseCaseImpl(
            SensorPort sensorPort,
            SensorReadingPort sensorReadingPort,
            AlertEvaluationService alertEvaluationService
    ) {
        this.sensorPort = sensorPort;
        this.sensorReadingPort = sensorReadingPort;
        this.alertEvaluationService = alertEvaluationService;
    }

    @Override
    public void simulateReadings() {
        Instant now = Instant.now();

        for (Sensor sensor : sensorPort.findAllActive()) {
            SensorReading reading = new SensorReading();
            reading.setSensorId(sensor.getId());
            reading.setValue(generateValueByType(sensor.getType()));
            reading.setRecordedAt(now);
            SensorReading saved = sensorReadingPort.save(reading);
            alertEvaluationService.evaluateReading(saved);
        }
    }

    private double generateValueByType(String sensorType) {
        String normalizedType = sensorType == null ? "" : sensorType.toUpperCase(Locale.ROOT);

        return switch (normalizedType) {
            case "TEMPERATURE" -> randomBetween(10.0, 40.0);
            case "SOIL_HUMIDITY" -> randomBetween(20.0, 90.0);
            case "AIR_HUMIDITY" -> randomBetween(30.0, 80.0);
            case "LUMINOSITY" -> randomBetween(100.0, 1000.0);
            default -> randomBetween(0.0, 100.0);
        };
    }

    private double randomBetween(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}

