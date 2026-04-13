package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPort {
    boolean existsByEmail(String email);
    boolean existsById(UUID id);
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    List<User> findAll();
}
