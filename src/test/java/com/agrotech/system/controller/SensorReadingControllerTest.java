package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.SensorReadingUseCase;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.dto.SensorReadingResponse;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SensorReadingController.class, ApiExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class SensorReadingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SensorReadingUseCase sensorReadingUseCase;

    @MockBean
    JwtService jwtService;

    @MockBean
    UserDetailsService userDetailsService;

    @Test
    void getLatestReading_comUsuarioValido_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        SensorReadingResponse response = new SensorReadingResponse(
                UUID.randomUUID(),
                sensorId,
                28.4,
                Instant.parse("2026-04-11T10:00:00Z"),
                Instant.parse("2026-04-11T10:00:10Z"),
                Map.of("source", "manual")
        );

        when(sensorReadingUseCase.getLatestReading(eq(sensorId), eq(userId), eq(Role.OPERADOR))).thenReturn(response);

        mockMvc.perform(get("/api/readings/{sensorId}/latest", sensorId)
                        .principal(auth(userId, Role.OPERADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorId").value(sensorId.toString()))
                .andExpect(jsonPath("$.value").value(28.4));
    }

    @Test
    void listReadings_comParametrosDePeriodo_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        when(sensorReadingUseCase.listReadings(
                eq(sensorId),
                eq(Instant.parse("2026-04-01T00:00:00Z")),
                eq(Instant.parse("2026-04-30T23:59:59Z")),
                eq(userId),
                eq(Role.OPERADOR)
        )).thenReturn(List.of(
                new SensorReadingResponse(
                        UUID.randomUUID(),
                        sensorId,
                        25.0,
                        Instant.parse("2026-04-10T08:00:00Z"),
                        Instant.parse("2026-04-10T08:00:01Z"),
                        Map.of()
                )
        ));

        mockMvc.perform(get("/api/readings")
                        .queryParam("sensorId", sensorId.toString())
                        .queryParam("startDate", "2026-04-01T00:00:00Z")
                        .queryParam("endDate", "2026-04-30T23:59:59Z")
                        .principal(auth(userId, Role.OPERADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sensorId").value(sensorId.toString()));
    }

    @Test
    void recordReading_delegaParaUseCaseComContextoDoUsuario() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        when(sensorReadingUseCase.recordReading(
                org.mockito.ArgumentMatchers.any(),
                eq(userId),
                eq(Role.OPERADOR)
        )).thenReturn(new SensorReadingResponse(
                UUID.randomUUID(),
                sensorId,
                22.0,
                Instant.parse("2026-04-11T12:00:00Z"),
                Instant.parse("2026-04-11T12:00:01Z"),
                Map.of()
        ));

        mockMvc.perform(post("/api/readings")
                        .principal(auth(userId, Role.OPERADOR))
                        .contentType("application/json")
                        .content("""
                                {
                                  "sensorId": "%s",
                                  "value": 22.0,
                                  "recordedAt": "2026-04-11T12:00:00Z",
                                  "data": {"source": "manual"}
                                }
                                """.formatted(sensorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sensorId").value(sensorId.toString()));
    }

    private Authentication auth(UUID userId, Role role) {
        AuthenticatedUser principal = new AuthenticatedUser(userId, "user@test.com", role);
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }
}
