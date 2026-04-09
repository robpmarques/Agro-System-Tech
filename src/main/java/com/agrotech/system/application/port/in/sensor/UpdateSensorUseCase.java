package com.agrotech.system.application.port.in.sensor;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface UpdateSensorUseCase {
    SensorOutput update(UpdateSensorCommand command, UUID currentUserId, Role currentRole);
}

