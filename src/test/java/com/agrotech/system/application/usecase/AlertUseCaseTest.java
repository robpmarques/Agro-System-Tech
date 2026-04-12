package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.out.AlertPort;
import com.agrotech.system.application.port.out.AlertRealtimePort;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Alert;
import com.agrotech.system.domain.model.AlertStatus;
import com.agrotech.system.domain.model.Area;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertUseCaseTest {

    @Mock
    private AlertPort alertPort;

    @Mock
    private SensorPort sensorPort;

    @Mock
    private AlertRealtimePort alertRealtimePort;

    @Mock
    private AreaRepositoryPort areaRepositoryPort;

    private AlertUseCase useCase;

    private UUID userId;
    private UUID areaId;
    private UUID sensorId;

    @BeforeEach
    void setUp() {
        useCase = new AlertUseCase(alertPort, alertRealtimePort, sensorPort, areaRepositoryPort);
        userId = UUID.randomUUID();
        areaId = UUID.randomUUID();
        sensorId = UUID.randomUUID();
    }

    @Test
    void list_comStatusActive_deveRetornarAlertasVisiveis() {
        mockVisibleSensor(Role.OPERADOR);

        Alert alert = new Alert(UUID.randomUUID(), sensorId, UUID.randomUUID(), 39.2, "msg", AlertStatus.ACTIVE, Instant.now(), null);
        when(alertPort.findAllByStatus(AlertStatus.ACTIVE)).thenReturn(List.of(alert));

        var output = useCase.list("ACTIVE", userId, Role.OPERADOR);

        assertEquals(1, output.size());
        assertEquals(AlertStatus.ACTIVE, output.get(0).status());
    }

    @Test
    void resolve_alertaAtivo_deveAtualizarParaResolvido() {
        UUID alertId = UUID.randomUUID();
        mockVisibleSensor(Role.OPERADOR);

        Alert active = new Alert(alertId, sensorId, UUID.randomUUID(), 22.0, "msg", AlertStatus.ACTIVE, Instant.now(), null);
        when(alertPort.findById(alertId)).thenReturn(Optional.of(active));
        when(alertPort.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var output = useCase.resolve(alertId, "RESOLVED", userId, Role.OPERADOR);

        assertEquals(AlertStatus.RESOLVED, output.status());
        assertNotNull(output.resolvedAt());
        verify(alertPort).save(any(Alert.class));
        verify(alertRealtimePort).publish(any());
    }

    @Test
    void list_especialista_deveLancarForbidden() {
        assertThrows(ForbiddenException.class, () -> useCase.list(null, userId, Role.ESPECIALISTA));
    }

    @Test
    void resolve_alertaInexistente_deveLancarNotFound() {
        UUID alertId = UUID.randomUUID();
        when(alertPort.findById(alertId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.resolve(alertId, "RESOLVED", userId, Role.OPERADOR));
    }

    private void mockVisibleSensor(Role role) {
        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(
                new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now())
        ));
        if (role == Role.ADMIN) {
            when(areaRepositoryPort.findById(areaId)).thenReturn(Optional.of(
                    Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())
            ));
        } else {
            when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.of(
                    Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())
            ));
        }
    }
}

