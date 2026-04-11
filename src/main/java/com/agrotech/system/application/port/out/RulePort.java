package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.Rule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RulePort {
    Rule save(Rule rule);
    List<Rule> findAllBySensorId(UUID sensorId);
    Optional<Rule> findById(UUID ruleId);
    void deleteById(UUID ruleId);
}

