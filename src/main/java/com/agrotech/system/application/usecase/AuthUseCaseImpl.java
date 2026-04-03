package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.AuthUseCase;
import com.agrotech.system.application.port.out.AccessTokenPort;
import com.agrotech.system.application.port.out.AuthenticationPort;
import com.agrotech.system.application.port.out.PasswordHashPort;
import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.domain.exception.ConflictException;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.exception.UnauthorizedException;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.User;
import com.agrotech.system.domain.service.UserDomainService;
import com.agrotech.system.dto.AuthResponse;

import java.util.List;

public class AuthUseCaseImpl implements AuthUseCase {

    private final UserPort userPort;
    private final PasswordHashPort passwordHashPort;
    private final AuthenticationPort authenticationPort;
    private final AccessTokenPort accessTokenPort;
    private final UserDomainService userDomainService;

    public AuthUseCaseImpl(
            UserPort userPort,
            PasswordHashPort passwordHashPort,
            AuthenticationPort authenticationPort,
            AccessTokenPort accessTokenPort,
            UserDomainService userDomainService
    ) {
        this.userPort = userPort;
        this.passwordHashPort = passwordHashPort;
        this.authenticationPort = authenticationPort;
        this.accessTokenPort = accessTokenPort;
        this.userDomainService = userDomainService;
    }

    @Override
    public AuthResponse register(String name, String email, String password, Role role) {
        if (userPort.existsByEmail(email)) {
            throw new ConflictException("Email ja cadastrado");
        }

        User user = userDomainService.createUser(name, email, passwordHashPort.hash(password), role);
        User savedUser = userPort.save(user);
        String accessToken = accessTokenPort.generateAccessToken(savedUser);

        return new AuthResponse(
                accessToken,
                "Bearer",
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    @Override
    public AuthResponse login(String email, String password) {
        authenticationPort.authenticate(email, password);

        User user = userPort.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Credenciais invalidas"));

        String accessToken = accessTokenPort.generateAccessToken(user);

        return new AuthResponse(
                accessToken,
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    public User me(String email) {
        return userPort.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));
    }

    @Override
    public List<User> listUsers() {
        return userPort.findAll();
    }
}
