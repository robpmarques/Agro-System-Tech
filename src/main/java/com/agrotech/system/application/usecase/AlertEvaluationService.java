package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.out.AlertPort;
import com.agrotech.system.application.port.out.RulePort;
import com.agrotech.system.domain.model.Alert;
import com.agrotech.system.domain.model.AlertStatus;
import com.agrotech.system.domain.model.Rule;
import com.agrotech.system.domain.model.SensorReading;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

public class AlertEvaluationService {

    private final RulePort rulePort;
    private final AlertPort alertPort;

    public AlertEvaluationService(RulePort rulePort, AlertPort alertPort) {
        this.rulePort = rulePort;
        this.alertPort = alertPort;
    }

    public void evaluateReading(SensorReading reading) {
        List<Rule> rules = rulePort.findAllBySensorId(reading.getSensorId());

        for (Rule rule : rules) {
            if (!rule.isActive()) {
                continue;
            }
            if (!matches(rule, reading.getValue())) {
                continue;
            }

            Alert alert = new Alert();
            alert.setSensorId(reading.getSensorId());
            alert.setRuleId(rule.getId());
            alert.setValue(reading.getValue());
            alert.setStatus(AlertStatus.ACTIVE);
            alert.setTriggeredAt(reading.getRecordedAt() != null ? reading.getRecordedAt() : Instant.now());
            alert.setMessage(buildMessage(rule, reading.getValue()));
            alertPort.save(alert);
        }
    }

    private boolean matches(Rule rule, Double value) {
        if (value == null || rule.getThreshold() == null || rule.getOperator() == null) {
            return false;
        }

        return switch (rule.getOperator().toUpperCase(Locale.ROOT)) {
            case "GT", ">" -> value > rule.getThreshold();
            case "GTE", ">=" -> value >= rule.getThreshold();
            case "LT", "<" -> value < rule.getThreshold();
            case "LTE", "<=" -> value <= rule.getThreshold();
            case "EQ", "=" -> Double.compare(value, rule.getThreshold()) == 0;
            case "NEQ", "!=" -> Double.compare(value, rule.getThreshold()) != 0;
            default -> false;
        };
    }

    private String buildMessage(Rule rule, Double value) {
        return "Regra '" + rule.getName() + "' violada: valor " + value + " " + rule.getOperator() + " " + rule.getThreshold();
    }
}

