package com.agrotech.system.domain.service;

import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.User;

public class UserDomainService {

    public User createUser(String name, String email, String hashedPassword, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole(role == null ? Role.OPERADOR : role);
        return user;
    }
}
