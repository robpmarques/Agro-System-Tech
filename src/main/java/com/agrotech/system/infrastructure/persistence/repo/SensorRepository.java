package com.agrotech.system.infrastructure.persistence.repo;

import com.agrotech.system.infrastructure.persistence.entity.SensorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SensorRepository extends JpaRepository<SensorEntity, UUID> {
}

