package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.sensor.CreateSensorCommand;
import com.agrotech.system.application.port.in.sensor.UpdateSensorCommand;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.exception.NotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorUseCaseTest {

    @Mock
    private SensorPort sensorPort;

    @Mock
    private AreaRepositoryPort areaRepositoryPort;

    private SensorUseCase sensorUseCase;

    private UUID userId;
    private UUID areaId;

    @BeforeEach
    void setUp() {
        sensorUseCase = new SensorUseCase(sensorPort, areaRepositoryPort);
        userId = UUID.randomUUID();
        areaId = UUID.randomUUID();
    }

    @Test
    void create_operadorComAreaValida_deveSalvarSensorAtivo() {
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));
        when(sensorPort.save(any(Sensor.class))).thenAnswer(invocation -> {
            Sensor sensor = invocation.getArgument(0);
            sensor.setId(UUID.randomUUID());
            return sensor;
        });

        var output = sensorUseCase.create(
                new CreateSensorCommand("Sensor 1", "temperature", "P1", areaId, null),
                userId,
                Role.OPERADOR
        );

        assertEquals("TEMPERATURE", output.type());
        assertTrue(output.isActive());
        assertEquals(areaId, output.areaId());
        verify(sensorPort).save(any(Sensor.class));
    }

    @Test
    void create_tipoInvalido_deveLancarErro() {
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));

        assertThrows(IllegalArgumentException.class, () -> sensorUseCase.create(
                new CreateSensorCommand("Sensor 1", "PRESSURE", "P1", areaId, true),
                userId,
                Role.OPERADOR
        ));

        verify(sensorPort, never()).save(any(Sensor.class));
    }

    @Test
    void create_perfilNulo_deveLancarForbidden() {
        assertThrows(ForbiddenException.class, () -> sensorUseCase.create(
                new CreateSensorCommand("Sensor 1", "TEMPERATURE", "P1", areaId, true),
                userId,
                null
        ));

        verify(sensorPort, never()).save(any(Sensor.class));
    }

    @Test
    void create_operadorComAreaDeOutroUsuario_deveLancarNotFound() {
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sensorUseCase.create(
                new CreateSensorCommand("Sensor 1", "TEMPERATURE", "P1", areaId, true),
                userId,
                Role.OPERADOR
        ));

        verify(sensorPort, never()).save(any(Sensor.class));
    }

    @Test
    void list_operador_deveRetornarSensoresDeSuasAreas() {
        UUID sensorId = UUID.randomUUID();
        when(areaRepositoryPort.findAreaIdsByUserId(userId)).thenReturn(List.of(areaId));
        when(sensorPort.findAllByAreaIds(List.of(areaId))).thenReturn(List.of(
                new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now())
        ));

        var result = sensorUseCase.list(userId, Role.OPERADOR);

        assertEquals(1, result.size());
        assertEquals(sensorId, result.get(0).id());
    }

    @Test
    void updateActivation_devePersistirNovoStatus() {
        UUID sensorId = UUID.randomUUID();
        Sensor sensor = new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now());
        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));
        when(sensorPort.save(any(Sensor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var output = sensorUseCase.updateActivation(sensorId, false, userId, Role.OPERADOR);

        assertTrue(!output.isActive());
        verify(sensorPort).save(any(Sensor.class));
    }

    @Test
    void delete_operadorComSensorVisivel_deveExcluir() {
        UUID sensorId = UUID.randomUUID();
        Sensor sensor = new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now());
        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));

        sensorUseCase.delete(sensorId, userId, Role.OPERADOR);

        verify(sensorPort).deleteById(sensorId);
    }

    @Test
    void update_operadorDeveAtualizarDados() {
        UUID sensorId = UUID.randomUUID();
        UUID newAreaId = UUID.randomUUID();
        Sensor existing = new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now());

        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(existing));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));
        when(areaRepositoryPort.findByIdAndUserId(newAreaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(newAreaId, "Area2", "Loc2", 12.0, userId, Instant.now())));
        when(sensorPort.save(any(Sensor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var output = sensorUseCase.update(
                new UpdateSensorCommand(sensorId, "Sensor Novo", "air_humidity", "P2", newAreaId, false),
                userId,
                Role.OPERADOR
        );

        assertEquals("AIR_HUMIDITY", output.type());
        assertEquals("Sensor Novo", output.name());
        assertEquals(newAreaId, output.areaId());
        assertTrue(!output.isActive());
    }
}

