package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.sensorplan.AssignSpecialistCommand;
import com.agrotech.system.application.port.in.sensorplan.AssignSpecialistUseCase;
import com.agrotech.system.application.port.in.sensorplan.CreatePlannedSensorCommand;
import com.agrotech.system.application.port.in.sensorplan.CreatePlannedSensorUseCase;
import com.agrotech.system.application.port.in.sensorplan.CreateSensorPlanCommand;
import com.agrotech.system.application.port.in.sensorplan.CreateSensorPlanUseCase;
import com.agrotech.system.application.port.in.sensorplan.ListPlannedSensorsUseCase;
import com.agrotech.system.application.port.in.sensorplan.ListSensorPlansUseCase;
import com.agrotech.system.application.port.in.sensorplan.PlannedSensorOutput;
import com.agrotech.system.application.port.in.sensorplan.SensorPlanOutput;
import com.agrotech.system.application.port.in.sensorplan.UpdateSensorPlanStatusCommand;
import com.agrotech.system.application.port.in.sensorplan.UpdateSensorPlanStatusUseCase;
import com.agrotech.system.dto.AssignSpecialistRequest;
import com.agrotech.system.dto.CreatePlannedSensorRequest;
import com.agrotech.system.dto.CreateSensorPlanRequest;
import com.agrotech.system.dto.PlannedSensorResponse;
import com.agrotech.system.dto.SensorPlanResponse;
import com.agrotech.system.dto.UpdateSensorPlanStatusRequest;
import com.agrotech.system.infrastructure.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sensor-plans")
public class SensorPlansController {

    private final CreateSensorPlanUseCase createSensorPlanUseCase;
    private final ListSensorPlansUseCase listSensorPlansUseCase;
    private final AssignSpecialistUseCase assignSpecialistUseCase;
    private final CreatePlannedSensorUseCase createPlannedSensorUseCase;
    private final ListPlannedSensorsUseCase listPlannedSensorsUseCase;
    private final UpdateSensorPlanStatusUseCase updateSensorPlanStatusUseCase;

    public SensorPlansController(
            CreateSensorPlanUseCase createSensorPlanUseCase,
            ListSensorPlansUseCase listSensorPlansUseCase,
            AssignSpecialistUseCase assignSpecialistUseCase,
            CreatePlannedSensorUseCase createPlannedSensorUseCase,
            ListPlannedSensorsUseCase listPlannedSensorsUseCase,
            UpdateSensorPlanStatusUseCase updateSensorPlanStatusUseCase
    ) {
        this.createSensorPlanUseCase = createSensorPlanUseCase;
        this.listSensorPlansUseCase = listSensorPlansUseCase;
        this.assignSpecialistUseCase = assignSpecialistUseCase;
        this.createPlannedSensorUseCase = createPlannedSensorUseCase;
        this.listPlannedSensorsUseCase = listPlannedSensorsUseCase;
        this.updateSensorPlanStatusUseCase = updateSensorPlanStatusUseCase;
    }

    @PostMapping
    public ResponseEntity<SensorPlanResponse> create(
            @Valid @RequestBody CreateSensorPlanRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);

        SensorPlanOutput output = createSensorPlanUseCase.create(
                new CreateSensorPlanCommand(request.areaId(), request.notes()),
                user.userId(),
                user.role()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(output));
    }

    @GetMapping
    public ResponseEntity<List<SensorPlanResponse>> list(
            @RequestParam UUID areaId,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        List<SensorPlanResponse> response = listSensorPlansUseCase.list(areaId, user.userId(), user.role()).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{planId}/assign-specialist")
    public ResponseEntity<SensorPlanResponse> assignSpecialist(
            @PathVariable UUID planId,
            @Valid @RequestBody AssignSpecialistRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);

        SensorPlanOutput output = assignSpecialistUseCase.assignSpecialist(
                new AssignSpecialistCommand(planId, request.specialistId()),
                user.userId(),
                user.role()
        );

        return ResponseEntity.ok(toResponse(output));
    }

    @PostMapping("/{planId}/planned-sensors")
    public ResponseEntity<PlannedSensorResponse> createPlannedSensor(
            @PathVariable UUID planId,
            @Valid @RequestBody CreatePlannedSensorRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);

        PlannedSensorOutput output = createPlannedSensorUseCase.createPlannedSensor(
                new CreatePlannedSensorCommand(planId, request.name(), request.type(), request.position()),
                user.userId(),
                user.role()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(output));
    }

    @GetMapping("/{planId}/planned-sensors")
    public ResponseEntity<List<PlannedSensorResponse>> listPlannedSensors(
            @PathVariable UUID planId,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        List<PlannedSensorResponse> response = listPlannedSensorsUseCase.listPlannedSensors(planId, user.userId(), user.role()).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{planId}/status")
    public ResponseEntity<SensorPlanResponse> updateStatus(
            @PathVariable UUID planId,
            @Valid @RequestBody UpdateSensorPlanStatusRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        SensorPlanOutput output = updateSensorPlanStatusUseCase.updateStatus(
                new UpdateSensorPlanStatusCommand(planId, request.status(), request.notes()),
                user.userId(),
                user.role()
        );
        return ResponseEntity.ok(toResponse(output));
    }

    private AuthenticatedUser extractCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado");
        }
        return user;
    }

    private SensorPlanResponse toResponse(SensorPlanOutput output) {
        return new SensorPlanResponse(
                output.id(),
                output.areaId(),
                output.requestedBy(),
                output.specialistId(),
                output.status(),
                output.notes(),
                output.createdAt(),
                output.reviewedAt()
        );
    }

    private PlannedSensorResponse toResponse(PlannedSensorOutput output) {
        return new PlannedSensorResponse(
                output.id(),
                output.planId(),
                output.name(),
                output.type(),
                output.position(),
                output.createdAt()
        );
    }
}

