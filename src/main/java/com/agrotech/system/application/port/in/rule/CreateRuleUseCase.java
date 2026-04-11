package com.agrotech.system.application.port.in.rule;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface CreateRuleUseCase {
    RuleOutput create(CreateRuleCommand command, UUID currentUserId, Role currentRole);
}

