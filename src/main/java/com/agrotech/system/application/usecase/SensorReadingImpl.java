package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.SensorReadingUseCase;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.application.port.out.SensorReadingPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.domain.model.SensorReading;
import com.agrotech.system.dto.SensorReadingRequest;
import com.agrotech.system.dto.SensorReadingResponse;

import java.util.UUID;

public class SensorReadingImpl implements SensorReadingUseCase {

    private final SensorReadingPort sensorReadingPort;
    private final SensorPort sensorPort;
    private final AreaRepositoryPort areaRepositoryPort;

    public SensorReadingImpl(
            SensorReadingPort sensorReadingPort,
            SensorPort sensorPort,
            AreaRepositoryPort areaRepositoryPort
    ) {
        this.sensorReadingPort = sensorReadingPort;
        this.sensorPort = sensorPort;
        this.areaRepositoryPort = areaRepositoryPort;
    }

    @Override
    public SensorReadingResponse recordReading(SensorReadingRequest request, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        Sensor sensor = sensorPort.findById(request.sensorId())
                .orElseThrow(() -> new NotFoundException("Sensor não encontrado"));

        ensureVisibleArea(sensor.getAreaId(), currentUserId, currentRole);

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

    private void ensureOperatorOrAdmin(Role role) {
        if (role != Role.OPERADOR && role != Role.ADMIN) {
            throw new ForbiddenException("Perfil sem permissao para registrar leituras manuais");
        }
    }

    private void ensureVisibleArea(UUID areaId, UUID currentUserId, Role currentRole) {
        if (currentRole == Role.ADMIN) {
            areaRepositoryPort.findById(areaId)
                    .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
            return;
        }

        areaRepositoryPort.findByIdAndUserId(areaId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Sensor nao encontrado"));
    }
}
