package com.agrotech.system.application.port.in.rule;

import java.util.UUID;

public record CreateRuleCommand(
        String name,
        String operator,
        Double threshold,
        Boolean isActive,
        UUID sensorId
) {
}

