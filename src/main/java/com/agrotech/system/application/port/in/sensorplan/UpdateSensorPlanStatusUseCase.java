package com.agrotech.system.application.port.in.sensorplan;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface UpdateSensorPlanStatusUseCase {
    SensorPlanOutput updateStatus(UpdateSensorPlanStatusCommand command, UUID currentUserId, Role currentRole);
}

