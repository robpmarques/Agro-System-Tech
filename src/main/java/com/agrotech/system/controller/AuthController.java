package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.AuthUseCase;
import com.agrotech.system.domain.model.User;
import com.agrotech.system.dto.AuthResponse;
import com.agrotech.system.dto.LoginRequest;
import com.agrotech.system.dto.RegisterRequest;
import com.agrotech.system.infrastructure.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authUseCase.register(request.name(), request.email(), request.password(), request.role()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authUseCase.login(request.email(), request.password()));
    }

    @GetMapping("/me")
    public ResponseEntity<User> me(Authentication authentication) {
        if (authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return ResponseEntity.ok(authUseCase.me(user.email()));
        }
        return ResponseEntity.ok(authUseCase.me(authentication.getName()));
    }
}

