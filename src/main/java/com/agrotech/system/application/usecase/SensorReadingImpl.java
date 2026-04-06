package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.SensorReadingUseCase;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.application.port.out.SensorReadingPort;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.domain.model.SensorReading;
import com.agrotech.system.dto.SensorReadingRequest;
import com.agrotech.system.dto.SensorReadingResponse;

public class SensorReadingImpl implements SensorReadingUseCase {

    private final SensorReadingPort sensorReadingPort;
    private final SensorPort sensorPort;

    public SensorReadingImpl(
            SensorReadingPort sensorReadingPort,
            SensorPort sensorPort
    ) {
        this.sensorReadingPort = sensorReadingPort;
        this.sensorPort = sensorPort;
    }

    @Override
    public SensorReadingResponse recordReading(SensorReadingRequest request) {
        Sensor sensor = sensorPort.findById(request.sensorId())
                .orElseThrow(() -> new NotFoundException("Sensor não encontrado"));

        if (!sensor.isActive()) {
            throw new NotFoundException("Sensor inativo");
        }

        SensorReading reading = new SensorReading();
        reading.setSensorId(request.sensorId());
        reading.setValue(request.value());
        reading.setRecordedAt(request.recordedAt());
        reading.setData(request.data());

        SensorReading savedReading = sensorReadingPort.save(reading);

        return new SensorReadingResponse(
                savedReading.getId(),
                savedReading.getSensorId(),
                savedReading.getValue(),
                savedReading.getRecordedAt(),
                savedReading.getCreatedAt(),
                savedReading.getData()
        );
    }
}
