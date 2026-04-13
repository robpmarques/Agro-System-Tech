package com.agrotech.system.application.port.in.sensorplan;

import java.util.UUID;

public record AssignSpecialistCommand(
        UUID planId,
        UUID specialistId
) {
}

