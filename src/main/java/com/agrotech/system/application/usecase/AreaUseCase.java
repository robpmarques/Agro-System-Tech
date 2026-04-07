package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.area.AreaOutput;
import com.agrotech.system.application.port.in.area.CreateAreaCommand;
import com.agrotech.system.application.port.in.area.CreateAreaUseCase;
import com.agrotech.system.application.port.in.area.DeleteAreaUseCase;
import com.agrotech.system.application.port.in.area.GetAreaByIdUseCase;
import com.agrotech.system.application.port.in.area.ListAreasQuery;
import com.agrotech.system.application.port.in.area.ListMyAreasUseCase;
import com.agrotech.system.application.port.in.area.PagedAreaOutput;
import com.agrotech.system.application.port.in.area.UpdateAreaCommand;
import com.agrotech.system.application.port.in.area.UpdateAreaUseCase;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Area;
import com.agrotech.system.domain.model.Role;

import java.util.List;
import java.util.UUID;

public class AreaUseCase implements
        CreateAreaUseCase,
        UpdateAreaUseCase,
        DeleteAreaUseCase,
        GetAreaByIdUseCase,
        ListMyAreasUseCase {

    private final AreaRepositoryPort areaRepositoryPort;

    public AreaUseCase(AreaRepositoryPort areaRepositoryPort) {
        this.areaRepositoryPort = areaRepositoryPort;
    }

    @Override
    public AreaOutput create(CreateAreaCommand command, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        Area saved = areaRepositoryPort.save(Area.create(command.name(), command.location(), command.size(), currentUserId));
        return toOutput(saved);
    }

    @Override
    public AreaOutput update(UpdateAreaCommand command, UUID currentUserId, Role currentRole) {
        Area area = findVisibleArea(command.areaId(), currentUserId, currentRole);
        area.updateDetails(command.name(), command.location(), command.size());
        return toOutput(areaRepositoryPort.save(area));
    }

    @Override
    public void delete(UUID areaId, UUID currentUserId, Role currentRole) {
        Area area = findVisibleArea(areaId, currentUserId, currentRole);
        areaRepositoryPort.delete(area);
    }

    @Override
    public AreaOutput getById(UUID areaId, UUID currentUserId, Role currentRole) {
        return toOutput(findVisibleArea(areaId, currentUserId, currentRole));
    }

    @Override
    public PagedAreaOutput list(ListAreasQuery query, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        boolean ascending = query.direction().equalsIgnoreCase("asc");

        List<Area> areas;
        long totalElements;

        if (currentRole == Role.ADMIN) {
            areas = areaRepositoryPort.findAll(query.page(), query.size(), query.sortBy(), ascending);
            totalElements = areaRepositoryPort.countAll();
        } else {
            areas = areaRepositoryPort.findAllByUserId(currentUserId, query.page(), query.size(), query.sortBy(), ascending);
            totalElements = areaRepositoryPort.countByUserId(currentUserId);
        }

        List<AreaOutput> output = areas.stream().map(this::toOutput).toList();
        int totalPages = (int) Math.ceil((double) totalElements / query.size());
        return new PagedAreaOutput(output, query.page(), query.size(), totalElements, totalPages);
    }

    private Area findVisibleArea(UUID areaId, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        if (currentRole == Role.ADMIN) {
            return areaRepositoryPort.findById(areaId)
                    .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
        }
        return areaRepositoryPort.findByIdAndUserId(areaId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
    }

    private void ensureOperatorOrAdmin(Role role) {
        if (role != Role.OPERADOR && role != Role.ADMIN) {
            throw new ForbiddenException("Perfil sem permissao para gerenciar areas");
        }
    }

    private AreaOutput toOutput(Area area) {
        return new AreaOutput(
                area.getId(),
                area.getName(),
                area.getLocation(),
                area.getSize(),
                area.getUserId(),
                area.getCreatedAt()
        );
    }
}

