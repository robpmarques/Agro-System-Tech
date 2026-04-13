package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.rule.CreateRuleCommand;
import com.agrotech.system.application.port.in.rule.UpdateRuleCommand;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.RulePort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.Area;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.Rule;
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
class RuleUseCaseTest {

    @Mock
    private RulePort rulePort;

    @Mock
    private SensorPort sensorPort;

    @Mock
    private AreaRepositoryPort areaRepositoryPort;

    private RuleUseCase ruleUseCase;

    private UUID userId;
    private UUID areaId;
    private UUID sensorId;
    private UUID ruleId;

    @BeforeEach
    void setUp() {
        ruleUseCase = new RuleUseCase(rulePort, sensorPort, areaRepositoryPort);
        userId = UUID.randomUUID();
        areaId = UUID.randomUUID();
        sensorId = UUID.randomUUID();
        ruleId = UUID.randomUUID();
    }

    @Test
    void create_operadorComSensorVisivel_deveSalvarRegra() {
        mockVisibleSensor(Role.OPERADOR);

        when(rulePort.save(any(Rule.class))).thenAnswer(invocation -> {
            Rule rule = invocation.getArgument(0);
            rule.setId(UUID.randomUUID());
            return rule;
        });

        var output = ruleUseCase.create(
                new CreateRuleCommand("Temperatura critica", "gt", 35.0, null, sensorId),
                userId,
                Role.OPERADOR
        );

        assertEquals("GT", output.operator());
        assertEquals(sensorId, output.sensorId());
        assertEquals(userId, output.userId());
        assertTrue(output.isActive());
        verify(rulePort).save(any(Rule.class));
    }

    @Test
    void create_operadorInvalido_deveLancarErro() {
        mockVisibleSensor(Role.OPERADOR);

        assertThrows(IllegalArgumentException.class, () -> ruleUseCase.create(
                new CreateRuleCommand("Regra", "between", 10.0, true, sensorId),
                userId,
                Role.OPERADOR
        ));

        verify(rulePort, never()).save(any(Rule.class));
    }

    @Test
    void create_perfilNulo_deveLancarForbidden() {
        assertThrows(ForbiddenException.class, () -> ruleUseCase.create(
                new CreateRuleCommand("Regra", "GT", 10.0, true, sensorId),
                userId,
                null
        ));

        verify(rulePort, never()).save(any(Rule.class));
    }

    @Test
    void listBySensor_sensorDeOutraArea_deveLancarNotFound() {
        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(
                new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now())
        ));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> ruleUseCase.listBySensor(sensorId, userId, Role.OPERADOR));
    }

    @Test
    void listBySensor_sensorVisivel_deveRetornarRegras() {
        mockVisibleSensor(Role.OPERADOR);

        when(rulePort.findAllBySensorId(sensorId)).thenReturn(List.of(
                new Rule(UUID.randomUUID(), "R1", "GT", 30.0, true, sensorId, userId, Instant.now())
        ));

        var result = ruleUseCase.listBySensor(sensorId, userId, Role.OPERADOR);

        assertEquals(1, result.size());
        assertEquals("R1", result.get(0).name());
    }

    @Test
    void update_regraVisivel_deveAtualizarDados() {
        mockVisibleRule();
        when(rulePort.save(any(Rule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var output = ruleUseCase.update(
                new UpdateRuleCommand(ruleId, "Nova Regra", "lte", 22.5, false),
                userId,
                Role.OPERADOR
        );

        assertEquals("Nova Regra", output.name());
        assertEquals("LTE", output.operator());
        assertEquals(22.5, output.threshold());
        assertTrue(!output.isActive());
    }

    @Test
    void delete_regraVisivel_deveExcluir() {
        mockVisibleRule();

        ruleUseCase.delete(ruleId, userId, Role.OPERADOR);

        verify(rulePort).deleteById(ruleId);
    }

    @Test
    void update_regraInexistente_deveLancarNotFound() {
        when(rulePort.findById(ruleId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> ruleUseCase.update(
                new UpdateRuleCommand(ruleId, "Regra", "GT", 10.0, true),
                userId,
                Role.OPERADOR
        ));
    }

    private void mockVisibleSensor(Role role) {
        when(sensorPort.findById(sensorId)).thenReturn(Optional.of(
                new Sensor(sensorId, "S1", "TEMPERATURE", "P1", areaId, true, Instant.now())
        ));
        if (role == Role.ADMIN) {
            when(areaRepositoryPort.findById(areaId))
                    .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));
        } else {
            when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                    .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));
        }
    }

    private void mockVisibleRule() {
        when(rulePort.findById(ruleId)).thenReturn(Optional.of(
                new Rule(ruleId, "Regra", "GT", 20.0, true, sensorId, userId, Instant.now())
        ));
        mockVisibleSensor(Role.OPERADOR);
    }
}

