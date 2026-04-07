package com.agrotech.system.application.port.in.area;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface UpdateAreaUseCase {
    AreaOutput update(UpdateAreaCommand command, UUID currentUserId, Role currentRole);
}

