package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.rule.CreateRuleCommand;
import com.agrotech.system.application.port.in.rule.CreateRuleUseCase;
import com.agrotech.system.application.port.in.rule.DeleteRuleUseCase;
import com.agrotech.system.application.port.in.rule.ListRulesBySensorUseCase;
import com.agrotech.system.application.port.in.rule.RuleOutput;
import com.agrotech.system.application.port.in.rule.UpdateRuleCommand;
import com.agrotech.system.application.port.in.rule.UpdateRuleUseCase;
import com.agrotech.system.dto.CreateRuleRequest;
import com.agrotech.system.dto.RuleResponse;
import com.agrotech.system.dto.UpdateRuleRequest;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class RulesController {

    private final CreateRuleUseCase createRuleUseCase;
    private final ListRulesBySensorUseCase listRulesBySensorUseCase;
    private final UpdateRuleUseCase updateRuleUseCase;
    private final DeleteRuleUseCase deleteRuleUseCase;

    public RulesController(
            CreateRuleUseCase createRuleUseCase,
            ListRulesBySensorUseCase listRulesBySensorUseCase,
            UpdateRuleUseCase updateRuleUseCase,
            DeleteRuleUseCase deleteRuleUseCase
    ) {
        this.createRuleUseCase = createRuleUseCase;
        this.listRulesBySensorUseCase = listRulesBySensorUseCase;
        this.updateRuleUseCase = updateRuleUseCase;
        this.deleteRuleUseCase = deleteRuleUseCase;
    }

    @PostMapping("/api/sensors/{sensorId}/rules")
    public ResponseEntity<RuleResponse> create(
            @PathVariable UUID sensorId,
            @Valid @RequestBody CreateRuleRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);

        RuleOutput output = createRuleUseCase.create(
                new CreateRuleCommand(
                        request.name(),
                        request.operator(),
                        request.threshold(),
                        request.isActive(),
                        sensorId
                ),
                user.userId(),
                user.role()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(output));
    }

    @GetMapping("/api/sensors/{sensorId}/rules")
    public ResponseEntity<List<RuleResponse>> listBySensor(@PathVariable UUID sensorId, Authentication authentication) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        List<RuleResponse> rules = listRulesBySensorUseCase.listBySensor(sensorId, user.userId(), user.role()).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(rules);
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<RuleResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRuleRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);

        RuleOutput output = updateRuleUseCase.update(
                new UpdateRuleCommand(
                        id,
                        request.name(),
                        request.operator(),
                        request.threshold(),
                        request.isActive()
                ),
                user.userId(),
                user.role()
        );

        return ResponseEntity.ok(toResponse(output));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        deleteRuleUseCase.delete(id, user.userId(), user.role());
        return ResponseEntity.noContent().build();
    }

    private AuthenticatedUser extractCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado");
        }
        return user;
    }

    private RuleResponse toResponse(RuleOutput output) {
        return new RuleResponse(
                output.id(),
                output.name(),
                output.operator(),
                output.threshold(),
                output.isActive(),
                output.sensorId(),
                output.userId(),
                output.createdAt()
        );
    }
}

