package com.agrotech.system.infrastructure.persistence.repo;

import com.agrotech.system.infrastructure.persistence.entity.SensorPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SensorPlanRepository extends JpaRepository<SensorPlanEntity, UUID> {
    List<SensorPlanEntity> findAllByAreaIdOrderByCreatedAtDesc(UUID areaId);

    List<SensorPlanEntity> findAllByRequestedByOrderByCreatedAtDesc(UUID requestedBy);
}

