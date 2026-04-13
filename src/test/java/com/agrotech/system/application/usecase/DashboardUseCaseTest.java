package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.DashboardPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.domain.model.SensorReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardUseCaseTest {

    @Mock
    private DashboardPort dashboardPort;

    @Mock
    private AreaRepositoryPort areaRepositoryPort;

    @Mock
    private SensorPort sensorPort;

    private DashboardUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DashboardUseCase(dashboardPort, areaRepositoryPort, sensorPort);
    }

    @Test
    void getSummary_admin_deveUsarEscopoGlobalSemFiltroPorDono() {
        UUID adminId = UUID.randomUUID();
        UUID areaA = UUID.randomUUID();
        UUID areaB = UUID.randomUUID();
        UUID sensorA = UUID.randomUUID();

        when(sensorPort.findAll()).thenReturn(List.of(
                new Sensor(UUID.randomUUID(), "S1", "TEMPERATURE", "NORTE", areaA, true, Instant.now()),
                new Sensor(UUID.randomUUID(), "S2", "AIR_HUMIDITY", "SUL", areaB, true, Instant.now())
        ));
        when(dashboardPort.findAverageReadingBySensorId(anyList())).thenReturn(Map.of(sensorA, 21.5));
        when(dashboardPort.findLatestReadings(anyList(), anyInt())).thenReturn(List.of(
                new SensorReading(UUID.randomUUID(), sensorA, 22.0, Instant.parse("2026-04-12T10:00:00Z"), Instant.now(), Map.of())
        ));
        when(dashboardPort.countActiveAlerts(anyList())).thenReturn(3L);
        when(dashboardPort.findActiveAlertIds(anyList(), anyInt())).thenReturn(List.of(UUID.randomUUID(), UUID.randomUUID()));

        var output = useCase.getSummary(adminId, Role.ADMIN);

        assertEquals(1, output.averageBySensorId().size());
        assertEquals(1, output.recentReadings().size());
        assertEquals(3L, output.activeAlertsTotal());
        assertEquals(2, output.activeAlertIds().size());

        verify(dashboardPort).findAverageReadingBySensorId(argThat(ids -> ids.size() == 2 && ids.containsAll(List.of(areaA, areaB))));
        verify(areaRepositoryPort, never()).findAreaIdsByUserId(adminId);
    }

    @Test
    void getSummary_operador_deveUsarApenasAreasProprias() {
        UUID operadorId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        when(areaRepositoryPort.findAreaIdsByUserId(operadorId)).thenReturn(List.of(areaId));
        when(dashboardPort.findAverageReadingBySensorId(List.of(areaId))).thenReturn(Map.of(sensorId, 25.1));
        when(dashboardPort.findLatestReadings(List.of(areaId), 10)).thenReturn(List.of(
                new SensorReading(UUID.randomUUID(), sensorId, 25.1, Instant.parse("2026-04-12T11:00:00Z"), Instant.now(), Map.of())
        ));
        when(dashboardPort.countActiveAlerts(List.of(areaId))).thenReturn(1L);
        when(dashboardPort.findActiveAlertIds(List.of(areaId), 20)).thenReturn(List.of(UUID.randomUUID()));

        var output = useCase.getSummary(operadorId, Role.OPERADOR);

        assertEquals(1, output.averageBySensorId().size());
        assertEquals(sensorId, output.recentReadings().getFirst().sensorId());
        assertEquals(1L, output.activeAlertsTotal());

        verify(areaRepositoryPort).findAreaIdsByUserId(operadorId);
        verify(sensorPort, never()).findAll();
    }

    @Test
    void getSummary_perfilInvalido_deveLancarForbidden() {
        UUID userId = UUID.randomUUID();
        assertThrows(ForbiddenException.class, () -> useCase.getSummary(userId, null));
    }

    @Test
    void getCharts_periodoInvalido_deveLancarIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        IllegalArgumentException missingPeriod = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.getCharts(sensorId, Instant.parse("2026-04-01T00:00:00Z"), null, userId, Role.OPERADOR)
        );
        assertEquals("startDate e endDate sao obrigatorios", missingPeriod.getMessage());

        IllegalArgumentException invalidRange = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.getCharts(
                        sensorId,
                        Instant.parse("2026-04-03T00:00:00Z"),
                        Instant.parse("2026-04-01T00:00:00Z"),
                        userId,
                        Role.OPERADOR
                )
        );
        assertEquals("endDate deve ser maior ou igual a startDate", invalidRange.getMessage());
    }

    @Test
    void getCharts_admin_deveRetornarSerieOrdenadaPorTimestamp() {
        UUID adminId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        Instant t1 = Instant.parse("2026-04-01T10:00:00Z");
        Instant t2 = Instant.parse("2026-04-01T09:00:00Z");

        when(sensorPort.findAll()).thenReturn(List.of(
                new Sensor(UUID.randomUUID(), "S1", "TEMPERATURE", "NORTE", areaId, true, Instant.now())
        ));
        when(dashboardPort.findSensorSeries(
                sensorId,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-02T00:00:00Z"),
                List.of(areaId)
        )).thenReturn(List.of(
                new SensorReading(UUID.randomUUID(), sensorId, 21.0, t1, Instant.now(), Map.of()),
                new SensorReading(UUID.randomUUID(), sensorId, 20.0, t2, Instant.now(), Map.of())
        ));

        var output = useCase.getCharts(
                sensorId,
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-02T00:00:00Z"),
                adminId,
                Role.ADMIN
        );

        assertEquals(2, output.size());
        assertEquals(t2, output.get(0).timestamp());
        assertEquals(t1, output.get(1).timestamp());
    }

    @Test
    void getCharts_operador_deveAplicarFiltroDasAreasDoUsuario() {
        UUID operadorId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        Instant start = Instant.parse("2026-04-01T00:00:00Z");
        Instant end = Instant.parse("2026-04-02T00:00:00Z");

        when(areaRepositoryPort.findAreaIdsByUserId(operadorId)).thenReturn(List.of(areaId));
        when(dashboardPort.findSensorSeries(sensorId, start, end, List.of(areaId))).thenReturn(List.of(
                new SensorReading(UUID.randomUUID(), sensorId, 19.5, Instant.parse("2026-04-01T12:00:00Z"), Instant.now(), Map.of())
        ));

        var output = useCase.getCharts(sensorId, start, end, operadorId, Role.OPERADOR);

        assertEquals(1, output.size());
        verify(areaRepositoryPort).findAreaIdsByUserId(operadorId);
        verify(sensorPort, never()).findAll();
    }
}

