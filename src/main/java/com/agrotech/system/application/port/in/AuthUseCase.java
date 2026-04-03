package com.agrotech.system.application.port.in;

import com.agrotech.system.dto.AuthResponse;
import com.agrotech.system.model.Role;
import com.agrotech.system.model.User;

import java.util.List;

public interface AuthUseCase {
    AuthResponse register(String name, String email, String password, Role role);
    AuthResponse login(String email, String password);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
    User me(String email);
    List<User> listUsers();
}
