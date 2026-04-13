package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.SensorPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorPlanPort {
    SensorPlan save(SensorPlan sensorPlan);

    Optional<SensorPlan> findById(UUID planId);

    List<SensorPlan> findAllByAreaId(UUID areaId);

    List<SensorPlan> findAllByRequestedBy(UUID requestedBy);

    void deleteById(UUID planId);
}

