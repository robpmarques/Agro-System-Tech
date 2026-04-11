package com.agrotech.system.application.port.in.rule;

import com.agrotech.system.domain.model.Role;

import java.util.List;
import java.util.UUID;

public interface ListRulesBySensorUseCase {
    List<RuleOutput> listBySensor(UUID sensorId, UUID currentUserId, Role currentRole);
}

