package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.PlannedSensor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlannedSensorPort {
    PlannedSensor save(PlannedSensor plannedSensor);

    Optional<PlannedSensor> findById(UUID plannedSensorId);

    List<PlannedSensor> findAllByPlanId(UUID planId);

    void deleteById(UUID plannedSensorId);

    void deleteAllByPlanId(UUID planId);
}

