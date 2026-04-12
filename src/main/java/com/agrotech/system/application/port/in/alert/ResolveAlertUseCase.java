package com.agrotech.system.application.port.in.alert;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface ResolveAlertUseCase {
    AlertOutput resolve(UUID alertId, String requestedStatus, UUID currentUserId, Role currentRole);
}

