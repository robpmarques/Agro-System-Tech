package com.agrotech.system.infrastructure.security;

import com.agrotech.system.application.port.out.AccessTokenPort;
import com.agrotech.system.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessTokenAdapter implements AccessTokenPort {

    private final JwtService jwtService;

    public JwtAccessTokenAdapter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String generateAccessToken(User user) {
        return jwtService.generateAccessToken(user.getEmail(), user.getRole(), user.getId());
    }
}
