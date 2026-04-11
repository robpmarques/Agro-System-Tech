package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.RulePort;
import com.agrotech.system.domain.model.Rule;
import com.agrotech.system.infrastructure.persistence.entity.RuleEntity;
import com.agrotech.system.infrastructure.persistence.entity.SensorEntity;
import com.agrotech.system.infrastructure.persistence.entity.UserEntity;
import com.agrotech.system.infrastructure.persistence.repo.RuleRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RulePersistenceAdapter implements RulePort {

    private final RuleRepository ruleRepository;
    private final EntityManager entityManager;

    public RulePersistenceAdapter(RuleRepository ruleRepository, EntityManager entityManager) {
        this.ruleRepository = ruleRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Rule save(Rule rule) {
        RuleEntity saved = ruleRepository.save(toEntity(rule));
        return toDomain(saved);
    }

    @Override
    public List<Rule> findAllBySensorId(UUID sensorId) {
        return ruleRepository.findBySensor_IdOrderByCreatedAtDesc(sensorId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Rule> findById(UUID ruleId) {
        return ruleRepository.findById(ruleId).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID ruleId) {
        ruleRepository.deleteById(ruleId);
    }

    private RuleEntity toEntity(Rule rule) {
        SensorEntity sensor = entityManager.getReference(SensorEntity.class, rule.getSensorId());
        UserEntity user = entityManager.getReference(UserEntity.class, rule.getUserId());

        RuleEntity entity = new RuleEntity();
        entity.setId(rule.getId());
        entity.setName(rule.getName());
        entity.setOperator(rule.getOperator());
        entity.setThreshold(rule.getThreshold());
        entity.setActive(rule.isActive());
        entity.setSensor(sensor);
        entity.setUser(user);
        entity.setCreatedAt(rule.getCreatedAt());
        return entity;
    }

    private Rule toDomain(RuleEntity entity) {
        return new Rule(
                entity.getId(),
                entity.getName(),
                entity.getOperator(),
                entity.getThreshold(),
                entity.getActive(),
                entity.getSensor().getId(),
                entity.getUser().getId(),
                entity.getCreatedAt()
        );
    }
}

