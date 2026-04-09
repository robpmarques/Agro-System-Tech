package com.agrotech.system.application.port.in.sensor;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface UpdateSensorActivationUseCase {
    SensorOutput updateActivation(UUID sensorId, boolean isActive, UUID currentUserId, Role currentRole);
}

