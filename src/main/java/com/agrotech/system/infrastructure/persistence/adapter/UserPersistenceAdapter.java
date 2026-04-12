package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.domain.model.User;
import com.agrotech.system.infrastructure.persistence.entity.UserEntity;
import com.agrotech.system.infrastructure.persistence.repo.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserPort {

    private final UserJpaRepository userRepository;

    public UserPersistenceAdapter(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        return toDomain(userRepository.save(entity));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private User toDomain(UserEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setName(entity.getName());
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        user.setRole(entity.getRole());
        return user;
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setRole(user.getRole());
        return entity;
    }
}
