package com.agrotech.system.application.port.in.sensorplan;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface CreatePlannedSensorUseCase {
    PlannedSensorOutput createPlannedSensor(CreatePlannedSensorCommand command, UUID currentUserId, Role currentRole);
}

