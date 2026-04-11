package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.rule.CreateRuleCommand;
import com.agrotech.system.application.port.in.rule.CreateRuleUseCase;
import com.agrotech.system.application.port.in.rule.DeleteRuleUseCase;
import com.agrotech.system.application.port.in.rule.ListRulesBySensorUseCase;
import com.agrotech.system.application.port.in.rule.RuleOutput;
import com.agrotech.system.application.port.in.rule.UpdateRuleCommand;
import com.agrotech.system.application.port.in.rule.UpdateRuleUseCase;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.RulePort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.Rule;
import com.agrotech.system.domain.model.Sensor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RuleUseCase implements CreateRuleUseCase, ListRulesBySensorUseCase, UpdateRuleUseCase, DeleteRuleUseCase {

    private static final Set<String> ALLOWED_OPERATORS = Set.of("GT", "GTE", "LT", "LTE", "EQ", "NEQ");

    private final RulePort rulePort;
    private final SensorPort sensorPort;
    private final AreaRepositoryPort areaRepositoryPort;

    public RuleUseCase(RulePort rulePort, SensorPort sensorPort, AreaRepositoryPort areaRepositoryPort) {
        this.rulePort = rulePort;
        this.sensorPort = sensorPort;
        this.areaRepositoryPort = areaRepositoryPort;
    }

    @Override
    public RuleOutput create(CreateRuleCommand command, UUID currentUserId, Role currentRole) {
        Sensor sensor = findVisibleSensor(command.sensorId(), currentUserId, currentRole);

        Rule rule = new Rule();
        rule.setName(normalizeRequired(command.name(), "Rule name is required"));
        rule.setOperator(normalizeOperator(command.operator()));
        rule.setThreshold(normalizeThreshold(command.threshold()));
        rule.setActive(command.isActive() == null || command.isActive());
        rule.setSensorId(sensor.getId());
        rule.setUserId(currentUserId);

        Rule saved = rulePort.save(rule);
        return toOutput(saved);
    }

    @Override
    public List<RuleOutput> listBySensor(UUID sensorId, UUID currentUserId, Role currentRole) {
        findVisibleSensor(sensorId, currentUserId, currentRole);
        return rulePort.findAllBySensorId(sensorId).stream().map(this::toOutput).toList();
    }

    @Override
    public RuleOutput update(UpdateRuleCommand command, UUID currentUserId, Role currentRole) {
        Rule rule = findVisibleRule(command.ruleId(), currentUserId, currentRole);

        rule.setName(normalizeRequired(command.name(), "Rule name is required"));
        rule.setOperator(normalizeOperator(command.operator()));
        rule.setThreshold(normalizeThreshold(command.threshold()));
        rule.setActive(command.isActive() == null || command.isActive());

        return toOutput(rulePort.save(rule));
    }

    @Override
    public void delete(UUID ruleId, UUID currentUserId, Role currentRole) {
        Rule rule = findVisibleRule(ruleId, currentUserId, currentRole);
        rulePort.deleteById(rule.getId());
    }

    private Sensor findVisibleSensor(UUID sensorId, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        if (sensorId == null) {
            throw new IllegalArgumentException("Sensor id is required");
        }

        Sensor sensor = sensorPort.findById(sensorId)
                .orElseThrow(() -> new NotFoundException("Sensor nao encontrado"));
        ensureVisibleArea(sensor.getAreaId(), currentUserId, currentRole);
        return sensor;
    }

    private Rule findVisibleRule(UUID ruleId, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        if (ruleId == null) {
            throw new IllegalArgumentException("Rule id is required");
        }

        Rule rule = rulePort.findById(ruleId)
                .orElseThrow(() -> new NotFoundException("Regra nao encontrada"));
        findVisibleSensor(rule.getSensorId(), currentUserId, currentRole);
        return rule;
    }

    private void ensureVisibleArea(UUID areaId, UUID currentUserId, Role currentRole) {
        if (currentRole == Role.ADMIN) {
            areaRepositoryPort.findById(areaId)
                    .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
            return;
        }

        areaRepositoryPort.findByIdAndUserId(areaId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
    }

    private void ensureOperatorOrAdmin(Role role) {
        if (role != Role.OPERADOR && role != Role.ADMIN) {
            throw new ForbiddenException("Perfil sem permissao para gerenciar regras");
        }
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOperator(String value) {
        String normalized = normalizeRequired(value, "Rule operator is required").toUpperCase();
        if (!ALLOWED_OPERATORS.contains(normalized)) {
            throw new IllegalArgumentException("Rule operator is invalid");
        }
        return normalized;
    }

    private Double normalizeThreshold(Double threshold) {
        if (threshold == null) {
            throw new IllegalArgumentException("Rule threshold is required");
        }
        return threshold;
    }

    private RuleOutput toOutput(Rule rule) {
        return new RuleOutput(
                rule.getId(),
                rule.getName(),
                rule.getOperator(),
                rule.getThreshold(),
                rule.isActive(),
                rule.getSensorId(),
                rule.getUserId(),
                rule.getCreatedAt()
        );
    }
}

