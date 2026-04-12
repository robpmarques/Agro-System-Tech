package com.agrotech.system.application.port.in.sensorplan;

import java.util.UUID;

public record CreateSensorPlanCommand(
        UUID areaId,
        String notes
) {
}

