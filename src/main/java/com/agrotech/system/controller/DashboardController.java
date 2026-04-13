package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.dashboard.DashboardChartPointOutput;
import com.agrotech.system.application.port.in.dashboard.DashboardSummaryOutput;
import com.agrotech.system.application.port.in.dashboard.GetDashboardChartsUseCase;
import com.agrotech.system.application.port.in.dashboard.GetDashboardSummaryUseCase;
import com.agrotech.system.dto.DashboardChartPointResponse;
import com.agrotech.system.dto.DashboardSummaryResponse;
import com.agrotech.system.infrastructure.security.AuthenticatedUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final GetDashboardSummaryUseCase getDashboardSummaryUseCase;
    private final GetDashboardChartsUseCase getDashboardChartsUseCase;

    public DashboardController(
            GetDashboardSummaryUseCase getDashboardSummaryUseCase,
            GetDashboardChartsUseCase getDashboardChartsUseCase
    ) {
        this.getDashboardSummaryUseCase = getDashboardSummaryUseCase;
        this.getDashboardChartsUseCase = getDashboardChartsUseCase;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(Authentication authentication) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        DashboardSummaryOutput output = getDashboardSummaryUseCase.getSummary(user.userId(), user.role());
        return ResponseEntity.ok(toSummaryResponse(output));
    }

    @GetMapping("/charts")
    public ResponseEntity<List<DashboardChartPointResponse>> getCharts(
            @RequestParam UUID sensorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            Authentication authentication
    ) {
        AuthenticatedUser user = extractCurrentUser(authentication);
        List<DashboardChartPointOutput> output = getDashboardChartsUseCase.getCharts(
                sensorId,
                startDate,
                endDate,
                user.userId(),
                user.role()
        );
        return ResponseEntity.ok(output.stream().map(this::toChartPointResponse).toList());
    }

    private AuthenticatedUser extractCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado");
        }
        return user;
    }

    private DashboardSummaryResponse toSummaryResponse(DashboardSummaryOutput output) {
        return new DashboardSummaryResponse(
                output.averageBySensorId(),
                output.recentReadings().stream()
                        .map(this::toChartPointResponse)
                        .toList(),
                output.activeAlertsTotal(),
                output.activeAlertIds()
        );
    }

    private DashboardChartPointResponse toChartPointResponse(DashboardChartPointOutput output) {
        return new DashboardChartPointResponse(
                output.sensorId(),
                output.timestamp(),
                output.value()
        );
    }
}

