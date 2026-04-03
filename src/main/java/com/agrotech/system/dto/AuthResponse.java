package com.agrotech.system.dto;

import com.agrotech.system.model.Role;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role
) {
}
