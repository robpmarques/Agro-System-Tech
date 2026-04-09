package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.sensor.CreateSensorCommand;
import com.agrotech.system.application.port.in.sensor.CreateSensorUseCase;
import com.agrotech.system.application.port.in.sensor.DeleteSensorUseCase;
import com.agrotech.system.application.port.in.sensor.GetSensorByIdUseCase;
import com.agrotech.system.application.port.in.sensor.ListSensorsUseCase;
import com.agrotech.system.application.port.in.sensor.SensorOutput;
import com.agrotech.system.application.port.in.sensor.UpdateSensorCommand;
import com.agrotech.system.application.port.in.sensor.UpdateSensorUseCase;
import com.agrotech.system.application.port.in.sensor.UpdateSensorActivationUseCase;
import com.agrotech.system.dto.UpdateSensorActivationRequest;
import com.agrotech.system.dto.UpdateSensorRequest;
import com.agrotech.system.dto.CreateSensorRequest;
import com.agrotech.system.dto.SensorResponse;
import com.agrotech.system.infrastructure.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sensors")
public class SensorsController {

    private final CreateSensorUseCase createSensorUseCase;
    private final ListSensorsUseCase listSensorsUseCase;
    private final GetSensorByIdUseCase getSensorByIdUseCase;
    private final UpdateSensorUseCase updateSensorUseCase;
    private final UpdateSensorActivationUseCase updateSensorActivationUseCase;
    private final DeleteSensorUseCase deleteSensorUseCase;

    public SensorsController(
            CreateSensorUseCase createSensorUseCase,
            ListSensorsUseCase listSensorsUseCase,
            GetSensorByIdUseCase getSensorByIdUseCase,
            UpdateSensorUseCase updateSensorUseCase,
            UpdateSensorActivationUseCase updateSensorActivationUseCase,
            DeleteSensorUseCase deleteSensorUseCase
    ) {
        this.createSensorUseCase = createSensorUseCase;
        this.listSensorsUseCase = listSensorsUseCase;
        this.getSensorByIdUseCase = getSensorByIdUseCase;
        this.updateSensorUseCase = updateSensorUseCase;
        this.updateSensorActivationUseCase = updateSensorActivationUseCase;
        this.deleteSensorUseCase = deleteSensorUseCase;
    }

    @GetMapping
    public ResponseEntity<List<SensorResponse>> list(Authentication authentication) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        List<SensorResponse> sensors = listSensorsUseCase.list(user.userId(), user.role()).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(sensors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorResponse> getById(@PathVariable UUID id, Authentication authentication) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        SensorOutput output = getSensorByIdUseCase.getById(id, user.userId(), user.role());
        return ResponseEntity.ok(toResponse(output));
    }

    @PostMapping
    public ResponseEntity<SensorResponse> create(@Valid @RequestBody CreateSensorRequest request, Authentication authentication) {
        AuthenticatedUser user = extractCurrentUser(authentication);

        SensorOutput output = createSensorUseCase.create(
                new CreateSensorCommand(
                        request.name(),
                        request.type(),
                        request.position(),
                        request.areaId(),
                        request.isActive()
                ),
                user.userId(),
                user.role()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(output));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSensorRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);

        SensorOutput output = updateSensorUseCase.update(
                new UpdateSensorCommand(
                        id,
                        request.name(),
                        request.type(),
                        request.position(),
                        request.areaId(),
                        request.isActive()
                ),
                user.userId(),
                user.role()
        );

        return ResponseEntity.ok(toResponse(output));
    }

    @PatchMapping("/{id}/activation")
    public ResponseEntity<SensorResponse> updateActivation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSensorActivationRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        SensorOutput output = updateSensorActivationUseCase.updateActivation(
                id,
                request.isActive(),
                user.userId(),
                user.role()
        );
        return ResponseEntity.ok(toResponse(output));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        deleteSensorUseCase.delete(id, user.userId(), user.role());
        return ResponseEntity.noContent().build();
    }

    private AuthenticatedUser extractCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado");
        }
        return user;
    }

    private SensorResponse toResponse(SensorOutput output) {
        return new SensorResponse(
                output.id(),
                output.name(),
                output.type(),
                output.position(),
                output.areaId(),
                output.isActive(),
                output.createdAt()
        );
    }
}

