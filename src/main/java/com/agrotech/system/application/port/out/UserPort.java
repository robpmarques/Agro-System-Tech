package com.agrotech.system.application.port.out;

import com.agrotech.system.model.User;

import java.util.List;
import java.util.Optional;

public interface UserPort {
    boolean existsByEmail(String email);
    User save(User user);
    Optional<User> findByEmail(String email);
    List<User> findAll();
}
