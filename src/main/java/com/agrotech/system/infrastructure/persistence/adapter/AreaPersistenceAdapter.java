package com.agrotech.system.infrastructure.persistence.adapter;

import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.domain.model.Area;
import com.agrotech.system.infrastructure.persistence.entity.AreaJpaEntity;
import com.agrotech.system.infrastructure.persistence.repo.AreaJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AreaPersistenceAdapter implements AreaRepositoryPort {

    private final AreaJpaRepository areaJpaRepository;

    public AreaPersistenceAdapter(AreaJpaRepository areaJpaRepository) {
        this.areaJpaRepository = areaJpaRepository;
    }

    @Override
    public Area save(Area area) {
        return toDomain(areaJpaRepository.save(toEntity(area)));
    }

    @Override
    public Optional<Area> findById(UUID id) {
        return areaJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Area> findByIdAndUserId(UUID id, UUID userId) {
        return areaJpaRepository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public List<Area> findAllByUserId(UUID userId, int page, int size, String sortBy, boolean ascending) {
        return areaJpaRepository.findAllByUserId(userId, pageRequest(page, size, sortBy, ascending))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(UUID userId) {
        return areaJpaRepository.countByUserId(userId);
    }

    @Override
    public List<Area> findAll(int page, int size, String sortBy, boolean ascending) {
        return areaJpaRepository.findAll(pageRequest(page, size, sortBy, ascending))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countAll() {
        return areaJpaRepository.count();
    }

    @Override
    public void delete(Area area) {
        areaJpaRepository.deleteById(area.getId());
    }

    private PageRequest pageRequest(int page, int size, String sortBy, boolean ascending) {
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, mapSort(sortBy));
        return PageRequest.of(page, size, sort);
    }

    private String mapSort(String sortBy) {
        if ("name".equalsIgnoreCase(sortBy)) {
            return "name";
        }
        if ("size".equalsIgnoreCase(sortBy)) {
            return "size";
        }
        return "createdAt";
    }

    private Area toDomain(AreaJpaEntity entity) {
        return Area.rehydrate(
                entity.getId(),
                entity.getName(),
                entity.getLocation(),
                entity.getSize(),
                entity.getUserId(),
                entity.getCreatedAt()
        );
    }

    private AreaJpaEntity toEntity(Area area) {
        AreaJpaEntity entity = new AreaJpaEntity();
        entity.setId(area.getId());
        entity.setName(area.getName());
        entity.setLocation(area.getLocation());
        entity.setSize(area.getSize());
        entity.setUserId(area.getUserId());
        entity.setCreatedAt(area.getCreatedAt());
        return entity;
    }
}

