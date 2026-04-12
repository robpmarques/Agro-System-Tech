package com.agrotech.system.application.port.in.sensorplan;

import java.util.UUID;

public record CreatePlannedSensorCommand(
        UUID planId,
        String name,
        String type,
        String position
) {
}

