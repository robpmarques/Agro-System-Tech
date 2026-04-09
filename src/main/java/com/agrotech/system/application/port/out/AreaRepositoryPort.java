package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.Area;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AreaRepositoryPort {
    Area save(Area area);
    Optional<Area> findById(UUID id);
    Optional<Area> findByIdAndUserId(UUID id, UUID userId);
    List<Area> findAllByUserId(UUID userId, int page, int size, String sortBy, boolean ascending);
    List<UUID> findAreaIdsByUserId(UUID userId);
    long countByUserId(UUID userId);
    List<Area> findAll(int page, int size, String sortBy, boolean ascending);
    long countAll();
    void delete(Area area);
}

