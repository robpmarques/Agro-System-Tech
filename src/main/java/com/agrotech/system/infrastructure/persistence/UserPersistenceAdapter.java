package com.agrotech.system.infrastructure.persistence;

import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.model.User;
import com.agrotech.system.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserPort {

    private final UserRepository userRepository;

    public UserPersistenceAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
}
