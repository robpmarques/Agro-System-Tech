package com.agrotech.system.controller;

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
import com.agrotech.system.dto.AreaResponse;
import com.agrotech.system.dto.CreateAreaRequest;
import com.agrotech.system.dto.PagedAreaResponse;
import com.agrotech.system.dto.UpdateAreaRequest;
import com.agrotech.system.infrastructure.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/areas")
public class AreasController {

    private final CreateAreaUseCase createAreaUseCase;
    private final UpdateAreaUseCase updateAreaUseCase;
    private final DeleteAreaUseCase deleteAreaUseCase;
    private final GetAreaByIdUseCase getAreaByIdUseCase;
    private final ListMyAreasUseCase listMyAreasUseCase;

    public AreasController(
            CreateAreaUseCase createAreaUseCase,
            UpdateAreaUseCase updateAreaUseCase,
            DeleteAreaUseCase deleteAreaUseCase,
            GetAreaByIdUseCase getAreaByIdUseCase,
            ListMyAreasUseCase listMyAreasUseCase
    ) {
        this.createAreaUseCase = createAreaUseCase;
        this.updateAreaUseCase = updateAreaUseCase;
        this.deleteAreaUseCase = deleteAreaUseCase;
        this.getAreaByIdUseCase = getAreaByIdUseCase;
        this.listMyAreasUseCase = listMyAreasUseCase;
    }

    @PostMapping
    public ResponseEntity<AreaResponse> create(@Valid @RequestBody CreateAreaRequest request, Authentication authentication) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        AreaOutput area = createAreaUseCase.create(
                new CreateAreaCommand(request.name(), request.location(), request.size()),
                user.userId(),
                user.role()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(area));
    }

    @GetMapping
    public ResponseEntity<PagedAreaResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        PagedAreaOutput output = listMyAreasUseCase.list(
                new ListAreasQuery(page, size, sort, direction),
                user.userId(),
                user.role()
        );
        return ResponseEntity.ok(toResponse(output));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaResponse> getById(@PathVariable UUID id, Authentication authentication) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        AreaOutput area = getAreaByIdUseCase.getById(id, user.userId(), user.role());
        return ResponseEntity.ok(toResponse(area));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAreaRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        AreaOutput area = updateAreaUseCase.update(
                new UpdateAreaCommand(id, request.name(), request.location(), request.size()),
                user.userId(),
                user.role()
        );
        return ResponseEntity.ok(toResponse(area));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        deleteAreaUseCase.delete(id, user.userId(), user.role());
        return ResponseEntity.noContent().build();
    }

    private AuthenticatedUser extractCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado");
        }
        return user;
    }

    private AreaResponse toResponse(AreaOutput output) {
        return new AreaResponse(
                output.id(),
                output.name(),
                output.location(),
                output.size(),
                output.userId(),
                output.createdAt()
        );
    }

    private PagedAreaResponse toResponse(PagedAreaOutput output) {
        return new PagedAreaResponse(
                output.content().stream().map(this::toResponse).toList(),
                output.page(),
                output.size(),
                output.totalElements(),
                output.totalPages()
        );
    }
}

