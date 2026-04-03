package com.agrotech.system.application.service;

import com.agrotech.system.application.exception.ConflictException;
import com.agrotech.system.application.exception.NotFoundException;
import com.agrotech.system.application.exception.UnauthorizedException;
import com.agrotech.system.application.port.in.AuthUseCase;
import com.agrotech.system.application.port.out.AccessTokenPort;
import com.agrotech.system.application.port.out.AuthenticationPort;
import com.agrotech.system.application.port.out.PasswordHashPort;
import com.agrotech.system.application.port.out.RefreshTokenPort;
import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.dto.AuthResponse;
import com.agrotech.system.model.RefreshToken;
import com.agrotech.system.model.Role;
import com.agrotech.system.model.User;

import java.util.List;
public class AuthApplicationService implements AuthUseCase {

    private final UserPort userPort;
    private final PasswordHashPort passwordHashPort;
    private final AuthenticationPort authenticationPort;
    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenPort refreshTokenPort;

    public AuthApplicationService(
            UserPort userPort,
            PasswordHashPort passwordHashPort,
            AuthenticationPort authenticationPort,
            AccessTokenPort accessTokenPort,
            RefreshTokenPort refreshTokenPort
    ) {
        this.userPort = userPort;
        this.passwordHashPort = passwordHashPort;
        this.authenticationPort = authenticationPort;
        this.accessTokenPort = accessTokenPort;
        this.refreshTokenPort = refreshTokenPort;
    }

    @Override
    public AuthResponse register(String name, String email, String password, Role role) {
        if (userPort.existsByEmail(email)) {
            throw new ConflictException("Email ja cadastrado");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordHashPort.hash(password));
        user.setRole(role == null ? Role.OPERADOR : role);

        User savedUser = userPort.save(user);
        String accessToken = accessTokenPort.generateAccessToken(savedUser);
        String refreshToken = refreshTokenPort.create(savedUser).getToken();

        return new AuthResponse(
                accessToken,
                refreshToken,
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
        String refreshToken = refreshTokenPort.create(user).getToken();

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        RefreshToken newRt = refreshTokenPort.rotate(refreshToken);
        User user = newRt.getUser();
        String accessToken = accessTokenPort.generateAccessToken(user);

        return new AuthResponse(
                accessToken,
                newRt.getToken(),
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenPort.revoke(refreshToken);
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
