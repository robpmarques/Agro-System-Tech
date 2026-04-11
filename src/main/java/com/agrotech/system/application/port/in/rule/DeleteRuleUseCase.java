package com.agrotech.system.application.port.in.rule;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface DeleteRuleUseCase {
    void delete(UUID ruleId, UUID currentUserId, Role currentRole);
}

