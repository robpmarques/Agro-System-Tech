package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.sensor.CreateSensorCommand;
import com.agrotech.system.application.port.in.sensor.CreateSensorUseCase;
import com.agrotech.system.application.port.in.sensor.DeleteSensorUseCase;
import com.agrotech.system.application.port.in.sensor.GetSensorByIdUseCase;
import com.agrotech.system.application.port.in.sensor.ListSensorsUseCase;
import com.agrotech.system.application.port.in.sensor.SensorOutput;
import com.agrotech.system.application.port.in.sensor.UpdateSensorActivationUseCase;
import com.agrotech.system.application.port.in.sensor.UpdateSensorCommand;
import com.agrotech.system.application.port.in.sensor.UpdateSensorUseCase;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.Sensor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SensorUseCase implements
        CreateSensorUseCase,
        ListSensorsUseCase,
        GetSensorByIdUseCase,
        UpdateSensorUseCase,
        UpdateSensorActivationUseCase,
        DeleteSensorUseCase {

    private static final Set<String> ALLOWED_SENSOR_TYPES = Set.of(
            "TEMPERATURE",
            "SOIL_HUMIDITY",
            "AIR_HUMIDITY",
            "LUMINOSITY"
    );

    private final SensorPort sensorPort;
    private final AreaRepositoryPort areaRepositoryPort;

    public SensorUseCase(SensorPort sensorPort, AreaRepositoryPort areaRepositoryPort) {
        this.sensorPort = sensorPort;
        this.areaRepositoryPort = areaRepositoryPort;
    }

    @Override
    public SensorOutput create(CreateSensorCommand command, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        ensureVisibleArea(command.areaId(), currentUserId, currentRole);

        Sensor sensor = new Sensor();
        sensor.setName(normalizeRequired(command.name(), "Sensor name is required"));
        sensor.setType(normalizeType(command.type()));
        sensor.setPosition(normalizeRequired(command.position(), "Sensor position is required"));
        sensor.setAreaId(command.areaId());
        sensor.setActive(command.isActive() == null || command.isActive());

        Sensor saved = sensorPort.save(sensor);
        return toOutput(saved);
    }

    @Override
    public List<SensorOutput> list(UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);

        if (currentRole == Role.ADMIN) {
            return sensorPort.findAll().stream().map(this::toOutput).toList();
        }

        List<UUID> areaIds = areaRepositoryPort.findAreaIdsByUserId(currentUserId);
        return sensorPort.findAllByAreaIds(areaIds).stream().map(this::toOutput).toList();
    }

    @Override
    public SensorOutput getById(UUID sensorId, UUID currentUserId, Role currentRole) {
        return toOutput(findVisibleSensor(sensorId, currentUserId, currentRole));
    }

    @Override
    public SensorOutput update(UpdateSensorCommand command, UUID currentUserId, Role currentRole) {
        Sensor sensor = findVisibleSensor(command.sensorId(), currentUserId, currentRole);
        ensureVisibleArea(command.areaId(), currentUserId, currentRole);

        sensor.setName(normalizeRequired(command.name(), "Sensor name is required"));
        sensor.setType(normalizeType(command.type()));
        sensor.setPosition(normalizeRequired(command.position(), "Sensor position is required"));
        sensor.setAreaId(command.areaId());
        sensor.setActive(command.isActive() == null || command.isActive());

        return toOutput(sensorPort.save(sensor));
    }

    @Override
    public SensorOutput updateActivation(UUID sensorId, boolean isActive, UUID currentUserId, Role currentRole) {
        Sensor sensor = findVisibleSensor(sensorId, currentUserId, currentRole);
        sensor.setActive(isActive);
        return toOutput(sensorPort.save(sensor));
    }

    @Override
    public void delete(UUID sensorId, UUID currentUserId, Role currentRole) {
        Sensor sensor = findVisibleSensor(sensorId, currentUserId, currentRole);
        sensorPort.deleteById(sensor.getId());
    }

    private Sensor findVisibleSensor(UUID sensorId, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        Sensor sensor = sensorPort.findById(sensorId)
                .orElseThrow(() -> new NotFoundException("Sensor nao encontrado"));
        ensureVisibleArea(sensor.getAreaId(), currentUserId, currentRole);
        return sensor;
    }

    private void ensureVisibleArea(UUID areaId, UUID currentUserId, Role currentRole) {
        if (areaId == null) {
            throw new IllegalArgumentException("Area id is required");
        }

        if (currentRole == Role.ADMIN) {
            areaRepositoryPort.findById(areaId)
                    .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
            return;
        }

        areaRepositoryPort.findByIdAndUserId(areaId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
    }

    private void ensureOperatorOrAdmin(Role role) {
        if (role != Role.OPERADOR && role != Role.ADMIN) {
            throw new ForbiddenException("Perfil sem permissao para gerenciar sensores");
        }
    }

    private String normalizeType(String value) {
        String normalized = normalizeRequired(value, "Sensor type is required").toUpperCase();
        if (!ALLOWED_SENSOR_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("Sensor type is invalid");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private SensorOutput toOutput(Sensor sensor) {
        return new SensorOutput(
                sensor.getId(),
                sensor.getName(),
                sensor.getType(),
                sensor.getPosition(),
                sensor.getAreaId(),
                sensor.isActive(),
                sensor.getCreatedAt()
        );
    }
}

