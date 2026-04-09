package com.agrotech.system.application.port.in.sensor;

import java.util.UUID;

public record UpdateSensorCommand(
        UUID sensorId,
        String name,
        String type,
        String position,
        UUID areaId,
        Boolean isActive
) {
}

