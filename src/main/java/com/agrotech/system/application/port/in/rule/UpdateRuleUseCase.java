package com.agrotech.system.application.port.in.rule;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface UpdateRuleUseCase {
    RuleOutput update(UpdateRuleCommand command, UUID currentUserId, Role currentRole);
}

