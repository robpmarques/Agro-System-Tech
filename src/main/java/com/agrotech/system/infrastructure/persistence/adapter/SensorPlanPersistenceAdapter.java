package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.SensorPlanPort;
import com.agrotech.system.domain.model.SensorPlan;
import com.agrotech.system.domain.model.SensorPlanStatus;
import com.agrotech.system.infrastructure.persistence.entity.SensorPlanEntity;
import com.agrotech.system.infrastructure.persistence.repo.SensorPlanRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SensorPlanPersistenceAdapter implements SensorPlanPort {

    private final SensorPlanRepository sensorPlanRepository;

    public SensorPlanPersistenceAdapter(SensorPlanRepository sensorPlanRepository) {
        this.sensorPlanRepository = sensorPlanRepository;
    }

    @Override
    public SensorPlan save(SensorPlan sensorPlan) {
        SensorPlanEntity saved = sensorPlanRepository.save(toEntity(sensorPlan));
        return toDomain(saved);
    }

    @Override
    public Optional<SensorPlan> findById(UUID planId) {
        return sensorPlanRepository.findById(planId).map(this::toDomain);
    }

    @Override
    public List<SensorPlan> findAllByAreaId(UUID areaId) {
        return sensorPlanRepository.findAllByAreaIdOrderByCreatedAtDesc(areaId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<SensorPlan> findAllByRequestedBy(UUID requestedBy) {
        return sensorPlanRepository.findAllByRequestedByOrderByCreatedAtDesc(requestedBy).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID planId) {
        sensorPlanRepository.deleteById(planId);
    }

    private SensorPlan toDomain(SensorPlanEntity entity) {
        return new SensorPlan(
                entity.getId(),
                entity.getAreaId(),
                entity.getRequestedBy(),
                entity.getSpecialistId(),
                parseStatus(entity.getStatus()),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getReviewedAt()
        );
    }

    private SensorPlanEntity toEntity(SensorPlan sensorPlan) {
        SensorPlanEntity entity = new SensorPlanEntity();
        entity.setId(sensorPlan.getId());
        entity.setAreaId(sensorPlan.getAreaId());
        entity.setRequestedBy(sensorPlan.getRequestedBy());
        entity.setSpecialistId(sensorPlan.getSpecialistId());
        entity.setStatus(sensorPlan.getStatus() == null ? null : sensorPlan.getStatus().name());
        entity.setNotes(sensorPlan.getNotes());
        entity.setCreatedAt(sensorPlan.getCreatedAt());
        entity.setReviewedAt(sensorPlan.getReviewedAt());
        return entity;
    }

    private SensorPlanStatus parseStatus(String status) {
        return status == null ? null : SensorPlanStatus.valueOf(status);
    }
}

