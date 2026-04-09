package com.agrotech.system.application.port.in.sensor;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface DeleteSensorUseCase {
    void delete(UUID sensorId, UUID currentUserId, Role currentRole);
}

