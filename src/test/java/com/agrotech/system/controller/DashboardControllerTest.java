package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.dashboard.DashboardChartPointOutput;
import com.agrotech.system.application.port.in.dashboard.DashboardSummaryOutput;
import com.agrotech.system.application.port.in.dashboard.GetDashboardChartsUseCase;
import com.agrotech.system.application.port.in.dashboard.GetDashboardSummaryUseCase;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {DashboardController.class, ApiExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    GetDashboardSummaryUseCase getDashboardSummaryUseCase;

    @MockBean
    GetDashboardChartsUseCase getDashboardChartsUseCase;

    @MockBean
    JwtService jwtService;

    @MockBean
    UserDetailsService userDetailsService;

    // --- GET /api/dashboard/summary ---

    @Test
    void getSummary_admin_retorna200ComResumo() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        when(getDashboardSummaryUseCase.getSummary(eq(adminId), eq(Role.ADMIN)))
                .thenReturn(new DashboardSummaryOutput(
                        Map.of(sensorId, 22.5),
                        List.of(new DashboardChartPointOutput(sensorId, Instant.parse("2026-04-12T10:00:00Z"), 23.0)),
                        2L,
                        List.of(UUID.randomUUID())
                ));

        mockMvc.perform(get("/api/dashboard/summary")
                        .principal(auth(adminId, Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageBySensorId['" + sensorId + "']").value(22.5))
                .andExpect(jsonPath("$.recentReadings[0].value").value(23.0))
                .andExpect(jsonPath("$.activeAlertsTotal").value(2))
                .andExpect(jsonPath("$.activeAlertIds.length()").value(1));
    }

    @Test
    void getSummary_operador_retorna200ComResumo() throws Exception {
        UUID operadorId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        when(getDashboardSummaryUseCase.getSummary(eq(operadorId), eq(Role.OPERADOR)))
                .thenReturn(new DashboardSummaryOutput(
                        Map.of(sensorId, 25.0),
                        List.of(),
                        0L,
                        List.of()
                ));

        mockMvc.perform(get("/api/dashboard/summary")
                        .principal(auth(operadorId, Role.OPERADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageBySensorId['" + sensorId + "']").value(25.0))
                .andExpect(jsonPath("$.activeAlertsTotal").value(0));
    }

    // --- GET /api/dashboard/charts ---

    @Test
    void getCharts_comParametrosValidos_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        Instant start = Instant.parse("2026-04-01T00:00:00Z");
        Instant end = Instant.parse("2026-04-02T00:00:00Z");

        when(getDashboardChartsUseCase.getCharts(
                eq(sensorId),
                eq(start),
                eq(end),
                eq(userId),
                eq(Role.OPERADOR)
        )).thenReturn(List.of(
                new DashboardChartPointOutput(sensorId, Instant.parse("2026-04-01T10:00:00Z"), 20.0),
                new DashboardChartPointOutput(sensorId, Instant.parse("2026-04-01T11:00:00Z"), 21.0)
        ));

        mockMvc.perform(get("/api/dashboard/charts")
                        .queryParam("sensorId", sensorId.toString())
                        .queryParam("startDate", "2026-04-01T00:00:00Z")
                        .queryParam("endDate", "2026-04-02T00:00:00Z")
                        .principal(auth(userId, Role.OPERADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].value").value(20.0))
                .andExpect(jsonPath("$[1].value").value(21.0));
    }


    @Test
    void getCharts_admin_comSerieOrdenada_retorna200() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        Instant t1 = Instant.parse("2026-04-01T09:00:00Z");
        Instant t2 = Instant.parse("2026-04-01T10:00:00Z");

        when(getDashboardChartsUseCase.getCharts(
                eq(sensorId),
                eq(Instant.parse("2026-04-01T00:00:00Z")),
                eq(Instant.parse("2026-04-02T00:00:00Z")),
                eq(adminId),
                eq(Role.ADMIN)
        )).thenReturn(List.of(
                new DashboardChartPointOutput(sensorId, t1, 20.0),
                new DashboardChartPointOutput(sensorId, t2, 21.0)
        ));

        mockMvc.perform(get("/api/dashboard/charts")
                        .queryParam("sensorId", sensorId.toString())
                        .queryParam("startDate", "2026-04-01T00:00:00Z")
                        .queryParam("endDate", "2026-04-02T00:00:00Z")
                        .principal(auth(adminId, Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].timestamp").value("2026-04-01T09:00:00Z"))
                .andExpect(jsonPath("$[1].timestamp").value("2026-04-01T10:00:00Z"));
    }

    private Authentication auth(UUID userId, Role role) {
        AuthenticatedUser principal = new AuthenticatedUser(userId, "user@test.com", role);
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }
}

