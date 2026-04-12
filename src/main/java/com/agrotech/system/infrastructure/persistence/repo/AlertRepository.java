package com.agrotech.system.infrastructure.persistence.repo;

import com.agrotech.system.domain.model.AlertStatus;
import com.agrotech.system.infrastructure.persistence.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {
    List<AlertEntity> findAllByOrderByTriggeredAtDesc();

    List<AlertEntity> findAllByStatusOrderByTriggeredAtDesc(AlertStatus status);
}

