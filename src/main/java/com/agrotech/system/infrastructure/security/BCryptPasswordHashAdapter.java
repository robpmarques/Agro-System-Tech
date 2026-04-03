package com.agrotech.system.infrastructure.security;

import com.agrotech.system.application.port.out.PasswordHashPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHashAdapter implements PasswordHashPort {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHashAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
