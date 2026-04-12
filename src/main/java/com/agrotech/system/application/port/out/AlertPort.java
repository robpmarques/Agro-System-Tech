package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.Alert;
import com.agrotech.system.domain.model.AlertStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertPort {
    Alert save(Alert alert);

    Optional<Alert> findById(UUID id);

    List<Alert> findAll();

    List<Alert> findAllByStatus(AlertStatus status);
}

