package com.agrotech.system.application.port.in;

import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.User;
import com.agrotech.system.dto.AuthResponse;

import java.util.List;

public interface AuthUseCase {
    AuthResponse register(String name, String email, String password, Role role);
    AuthResponse login(String email, String password);
    User me(String email);
    List<User> listUsers();
}
