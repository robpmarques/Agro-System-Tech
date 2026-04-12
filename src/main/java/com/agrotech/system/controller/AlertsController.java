package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.alert.AlertOutput;
import com.agrotech.system.application.port.in.alert.ListAlertsUseCase;
import com.agrotech.system.application.port.in.alert.ResolveAlertUseCase;
import com.agrotech.system.dto.AlertResponse;
import com.agrotech.system.dto.ResolveAlertRequest;
import com.agrotech.system.infrastructure.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertsController {

    private final ListAlertsUseCase listAlertsUseCase;
    private final ResolveAlertUseCase resolveAlertUseCase;

    public AlertsController(ListAlertsUseCase listAlertsUseCase, ResolveAlertUseCase resolveAlertUseCase) {
        this.listAlertsUseCase = listAlertsUseCase;
        this.resolveAlertUseCase = resolveAlertUseCase;
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> list(
            @RequestParam(required = false) String status,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        List<AlertResponse> response = listAlertsUseCase.list(status, user.userId(), user.role()).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertResponse> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveAlertRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        AlertOutput output = resolveAlertUseCase.resolve(id, request.status(), user.userId(), user.role());
        return ResponseEntity.ok(toResponse(output));
    }

    private AuthenticatedUser extractCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado");
        }
        return user;
    }

    private AlertResponse toResponse(AlertOutput output) {
        return new AlertResponse(
                output.id(),
                output.sensorId(),
                output.ruleId(),
                output.value(),
                output.message(),
                output.status(),
                output.triggeredAt(),
                output.resolvedAt()
        );
    }
}

