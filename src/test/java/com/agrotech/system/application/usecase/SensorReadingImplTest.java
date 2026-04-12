package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.application.port.out.SensorReadingPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Area;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.domain.model.SensorReading;
import com.agrotech.system.dto.SensorReadingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorReadingImplTest {

    @Mock
    private SensorReadingPort sensorReadingPort;

    @Mock
    private SensorPort sensorPort;

    @Mock
    private AreaRepositoryPort areaRepositoryPort;

    @Mock
    private AlertEvaluationService alertEvaluationService;

    private SensorReadingImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SensorReadingImpl(sensorReadingPort, sensorPort, areaRepositoryPort, alertEvaluationService);
    }

    @Test
    void recordReading_operadorComAreaVisivel_deveSalvarLeitura() {
        UUID userId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        Instant recordedAt = Instant.now();

        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(
                new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now())
        ));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.of(
                Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())
        ));
        when(sensorReadingPort.save(any(SensorReading.class))).thenAnswer(invocation -> {
            SensorReading reading = invocation.getArgument(0);
            reading.setId(UUID.randomUUID());
            reading.setCreatedAt(Instant.now());
            return reading;
        });

        var response = useCase.recordReading(
                new SensorReadingRequest(sensorId, 27.5, recordedAt, Map.of("source", "manual")),
                userId,
                Role.OPERADOR
        );

        assertEquals(sensorId, response.sensorId());
        assertEquals(27.5, response.value());
        verify(sensorReadingPort).save(any(SensorReading.class));
        verify(alertEvaluationService).evaluateReading(any(SensorReading.class));
    }

    @Test
    void recordReading_operadorSemOwnership_deveLancarNotFound() {
        UUID userId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(
                new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now())
        ));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.recordReading(
                new SensorReadingRequest(sensorId, 27.5, Instant.now(), Map.of()),
                userId,
                Role.OPERADOR
        ));

        verify(sensorReadingPort, never()).save(any(SensorReading.class));
    }

    @Test
    void recordReading_especialista_deveLancarForbidden() {
        assertThrows(ForbiddenException.class, () -> useCase.recordReading(
                new SensorReadingRequest(UUID.randomUUID(), 25.0, Instant.now(), Map.of()),
                UUID.randomUUID(),
                Role.ESPECIALISTA
        ));

        verify(sensorPort, never()).findById(any());
        verify(sensorReadingPort, never()).save(any(SensorReading.class));
    }

    @Test
    void getLatestReading_operadorComSensorVisivel_deveRetornarLeitura() {
        UUID userId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();

        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(
                new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now())
        ));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.of(
                Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())
        ));
        when(sensorReadingPort.findLatestBySensorId(sensorId)).thenReturn(Optional.of(
                new SensorReading(UUID.randomUUID(), sensorId, 31.2, Instant.now(), Instant.now(), Map.of())
        ));

        var response = useCase.getLatestReading(sensorId, userId, Role.OPERADOR);

        assertEquals(sensorId, response.sensorId());
        assertEquals(31.2, response.value());
    }

    @Test
    void listReadings_comPeriodo_deveRetornarLeiturasOrdenadasDoPort() {
        UUID userId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        Instant startDate = Instant.parse("2026-04-01T00:00:00Z");
        Instant endDate = Instant.parse("2026-04-30T23:59:59Z");

        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(
                new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now())
        ));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.of(
                Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())
        ));
        when(sensorReadingPort.findBySensorIdAndRecordedAtBetween(sensorId, startDate, endDate)).thenReturn(List.of(
                new SensorReading(UUID.randomUUID(), sensorId, 20.0, Instant.parse("2026-04-10T10:00:00Z"), Instant.now(), Map.of()),
                new SensorReading(UUID.randomUUID(), sensorId, 18.0, Instant.parse("2026-04-09T10:00:00Z"), Instant.now(), Map.of())
        ));

        var response = useCase.listReadings(sensorId, startDate, endDate, userId, Role.OPERADOR);

        assertEquals(2, response.size());
        assertTrue(response.get(0).recordedAt().isAfter(response.get(1).recordedAt()));
    }

    @Test
    void listReadings_periodoInvalido_deveLancarIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID sensorId = UUID.randomUUID();
        Instant startDate = Instant.parse("2026-04-10T00:00:00Z");
        Instant endDate = Instant.parse("2026-04-09T00:00:00Z");

        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(
                new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now())
        ));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.of(
                Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())
        ));

        assertThrows(IllegalArgumentException.class, () ->
                useCase.listReadings(sensorId, startDate, endDate, userId, Role.OPERADOR)
        );
    }
}

