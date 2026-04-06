package com.agrotech.system.dto;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UUID userId,
        String name,
        String email,
        Role role
) {
}
