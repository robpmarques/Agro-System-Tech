package com.agrotech.system.application.port.in.rule;

import java.util.UUID;

public record UpdateRuleCommand(
        UUID ruleId,
        String name,
        String operator,
        Double threshold,
        Boolean isActive
) {
}

