package com.agrotech.system.application.port.in.alert;

import com.agrotech.system.domain.model.Role;

import java.util.List;
import java.util.UUID;

public interface ListAlertsUseCase {
    List<AlertOutput> list(String status, UUID currentUserId, Role currentRole);
}

