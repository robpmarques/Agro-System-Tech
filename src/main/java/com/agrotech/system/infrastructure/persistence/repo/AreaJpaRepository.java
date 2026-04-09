package com.agrotech.system.infrastructure.persistence.repo;

import com.agrotech.system.infrastructure.persistence.entity.AreaJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AreaJpaRepository extends JpaRepository<AreaJpaEntity, UUID> {
    Optional<AreaJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    Page<AreaJpaEntity> findAllByUserId(UUID userId, Pageable pageable);
    long countByUserId(UUID userId);
}

