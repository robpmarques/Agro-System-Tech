package com.agrotech.system.application.port.in.sensor;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface CreateSensorUseCase {
    SensorOutput create(CreateSensorCommand command, UUID currentUserId, Role currentRole);
}

