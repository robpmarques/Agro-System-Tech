package com.agrotech.system.dto;

import com.agrotech.system.domain.model.Role;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role
) {
}
