package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.alert.AlertOutput;
import com.agrotech.system.application.port.in.alert.ListAlertsUseCase;
import com.agrotech.system.application.port.in.alert.ResolveAlertUseCase;
import com.agrotech.system.domain.model.AlertStatus;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.infrastructure.security.AuthenticatedUser;
import com.agrotech.system.infrastructure.security.JwtService;
import com.agrotech.system.infrastructure.web.error.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AlertsController.class, ApiExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class AlertsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ListAlertsUseCase listAlertsUseCase;

    @MockBean
    ResolveAlertUseCase resolveAlertUseCase;

    @MockBean
    JwtService jwtService;

    @MockBean
    UserDetailsService userDetailsService;

    @Test
    void list_comStatusActive_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        when(listAlertsUseCase.list(eq("ACTIVE"), eq(userId), eq(Role.OPERADOR))).thenReturn(List.of(
                new AlertOutput(
                        UUID.randomUUID(),
                        sensorId,
                        ruleId,
                        37.0,
                        "msg",
                        AlertStatus.ACTIVE,
                        Instant.parse("2026-04-11T12:00:00Z"),
                        null
                )
        ));

        mockMvc.perform(get("/api/alerts")
                        .queryParam("status", "ACTIVE")
                        .principal(auth(userId, Role.OPERADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void resolve_comPayloadValido_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID alertId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        when(resolveAlertUseCase.resolve(eq(alertId), eq("RESOLVED"), eq(userId), eq(Role.OPERADOR)))
                .thenReturn(new AlertOutput(
                        alertId,
                        sensorId,
                        ruleId,
                        28.0,
                        "msg",
                        AlertStatus.RESOLVED,
                        Instant.parse("2026-04-11T12:00:00Z"),
                        Instant.parse("2026-04-11T12:05:00Z")
                ));

        mockMvc.perform(put("/api/alerts/{id}", alertId)
                        .principal(auth(userId, Role.OPERADOR))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"status":"RESOLVED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alertId.toString()))
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    private Authentication auth(UUID userId, Role role) {
        AuthenticatedUser principal = new AuthenticatedUser(userId, "user@test.com", role);
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }
}

