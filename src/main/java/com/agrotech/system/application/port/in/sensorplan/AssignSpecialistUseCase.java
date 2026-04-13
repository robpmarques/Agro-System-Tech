package com.agrotech.system.application.port.in.sensorplan;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface AssignSpecialistUseCase {
    SensorPlanOutput assignSpecialist(AssignSpecialistCommand command, UUID currentUserId, Role currentRole);
}

