package com.agrotech.system.application.port.in.sensorplan;

import java.util.UUID;

public record UpdateSensorPlanStatusCommand(
        UUID planId,
        String status,
        String notes
) {
}

