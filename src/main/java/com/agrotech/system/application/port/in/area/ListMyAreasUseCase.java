package com.agrotech.system.application.port.in.area;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface ListMyAreasUseCase {
    PagedAreaOutput list(ListAreasQuery query, UUID currentUserId, Role currentRole);
}

