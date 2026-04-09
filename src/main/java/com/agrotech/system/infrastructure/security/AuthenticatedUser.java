package com.agrotech.system.infrastructure.security;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String email,
        Role role
) {
}

