package com.agrotech.system.application.port.in.sensorplan;

import com.agrotech.system.domain.model.Role;

import java.util.List;
import java.util.UUID;

public interface ListSensorPlansUseCase {
    List<SensorPlanOutput> list(UUID areaId, UUID currentUserId, Role currentRole);
}

