package com.agrotech.system.application.port.in.area;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface DeleteAreaUseCase {
    void delete(UUID areaId, UUID currentUserId, Role currentRole);
}

