package com.agrotech.system.application.port.in.sensor;

import com.agrotech.system.domain.model.Role;

import java.util.List;
import java.util.UUID;

public interface ListSensorsUseCase {
    List<SensorOutput> list(UUID currentUserId, Role currentRole);
}

