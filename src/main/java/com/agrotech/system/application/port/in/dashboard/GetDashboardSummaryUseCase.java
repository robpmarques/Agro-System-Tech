package com.agrotech.system.application.port.in.dashboard;

import com.agrotech.system.domain.model.Role;

import java.util.UUID;

public interface GetDashboardSummaryUseCase {
    DashboardSummaryOutput getSummary(UUID currentUserId, Role currentRole);
}

