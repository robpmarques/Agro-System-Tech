package com.agrotech.system.infrastructure.persistence.repo;

import com.agrotech.system.infrastructure.persistence.entity.PlannedSensorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlannedSensorRepository extends JpaRepository<PlannedSensorEntity, UUID> {
    List<PlannedSensorEntity> findAllByPlanIdOrderByCreatedAtDesc(UUID planId);

    void deleteAllByPlanId(UUID planId);
}

