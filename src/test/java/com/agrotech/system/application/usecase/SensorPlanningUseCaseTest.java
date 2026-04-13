package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.sensorplan.AssignSpecialistCommand;
import com.agrotech.system.application.port.in.sensorplan.CreatePlannedSensorCommand;
import com.agrotech.system.application.port.in.sensorplan.CreateSensorPlanCommand;
import com.agrotech.system.application.port.in.sensorplan.UpdateSensorPlanStatusCommand;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.PlannedSensorPort;
import com.agrotech.system.application.port.out.SensorPlanPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.model.Area;
import com.agrotech.system.domain.model.PlannedSensor;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.domain.model.SensorPlan;
import com.agrotech.system.domain.model.SensorPlanStatus;
import com.agrotech.system.domain.model.SensorPosition;
import com.agrotech.system.domain.model.SensorType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorPlanningUseCaseTest {

    @Mock
    private SensorPlanPort sensorPlanPort;

    @Mock
    private PlannedSensorPort plannedSensorPort;

    @Mock
    private SensorPort sensorPort;

    @Mock
    private AreaRepositoryPort areaRepositoryPort;

    @Mock
    private UserPort userPort;

    private SensorPlanningUseCase useCase;

    private UUID userId;
    private UUID areaId;

    @BeforeEach
    void setUp() {
        useCase = new SensorPlanningUseCase(sensorPlanPort, plannedSensorPort, sensorPort, areaRepositoryPort, userPort);
        userId = UUID.randomUUID();
        areaId = UUID.randomUUID();
    }

    @Test
    void create_operadorComAreaVisivel_deveCriarPlanoPendente() {
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));
        when(sensorPlanPort.save(any(SensorPlan.class))).thenAnswer(invocation -> {
            SensorPlan plan = invocation.getArgument(0);
            plan.setId(UUID.randomUUID());
            return plan;
        });

        var output = useCase.create(new CreateSensorPlanCommand(areaId, "Plano inicial"), userId, Role.OPERADOR);

        assertEquals(SensorPlanStatus.PENDING, output.status());
        assertEquals(areaId, output.areaId());
        assertEquals(userId, output.requestedBy());
    }

    @Test
    void create_perfilNulo_deveLancarForbidden() {
        assertThrows(ForbiddenException.class,
                () -> useCase.create(new CreateSensorPlanCommand(areaId, null), userId, null));

        verify(sensorPlanPort, never()).save(any(SensorPlan.class));
    }

    @Test
    void assignSpecialist_deveAtualizarPlanoParaInProgress() {
        UUID planId = UUID.randomUUID();
        UUID specialistId = UUID.randomUUID();
        SensorPlan plan = buildPlan(planId, SensorPlanStatus.PENDING, null);

        when(sensorPlanPort.findById(planId)).thenReturn(Optional.of(plan));
        when(userPort.existsById(specialistId)).thenReturn(true);
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));
        when(sensorPlanPort.save(any(SensorPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var output = useCase.assignSpecialist(new AssignSpecialistCommand(planId, specialistId), userId, Role.OPERADOR);

        assertEquals(SensorPlanStatus.IN_PROGRESS, output.status());
        assertEquals(specialistId, output.specialistId());
    }

    @Test
    void createPlannedSensor_deveSalvarComTipoEPosicaoValidos() {
        UUID planId = UUID.randomUUID();
        SensorPlan plan = buildPlan(planId, SensorPlanStatus.IN_PROGRESS, userId);

        when(sensorPlanPort.findById(planId)).thenReturn(Optional.of(plan));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));
        when(plannedSensorPort.save(any(PlannedSensor.class))).thenAnswer(invocation -> {
            PlannedSensor planned = invocation.getArgument(0);
            planned.setId(UUID.randomUUID());
            return planned;
        });

        var output = useCase.createPlannedSensor(
                new CreatePlannedSensorCommand(planId, "PS-1", "TEMPERATURE", "NORTE"),
                userId,
                Role.OPERADOR
        );

        assertEquals(SensorType.TEMPERATURE, output.type());
        assertEquals(SensorPosition.NORTE, output.position());
        assertEquals("PS-1", output.name());
    }

    @Test
    void updateStatus_approved_deveConverterPlannedSensorsEmSensors() {
        UUID planId = UUID.randomUUID();
        SensorPlan plan = buildPlan(planId, SensorPlanStatus.IN_PROGRESS, userId);

        PlannedSensor planned = new PlannedSensor(
                UUID.randomUUID(),
                planId,
                "Planejado 1",
                SensorType.TEMPERATURE,
                SensorPosition.CENTRO,
                Instant.now()
        );

        when(sensorPlanPort.findById(planId)).thenReturn(Optional.of(plan));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));
        when(plannedSensorPort.findAllByPlanId(planId)).thenReturn(List.of(planned));
        when(sensorPort.save(any(Sensor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sensorPlanPort.save(any(SensorPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var output = useCase.updateStatus(
                new UpdateSensorPlanStatusCommand(planId, "APPROVED", "ok"),
                userId,
                Role.OPERADOR
        );

        assertEquals(SensorPlanStatus.APPROVED, output.status());
        verify(sensorPort, times(1)).save(any(Sensor.class));
    }

    @Test
    void updateStatus_rejected_naoDeveCriarSensorReal() {
        UUID planId = UUID.randomUUID();
        SensorPlan plan = buildPlan(planId, SensorPlanStatus.IN_PROGRESS, userId);

        when(sensorPlanPort.findById(planId)).thenReturn(Optional.of(plan));
        when(areaRepositoryPort.findByIdAndUserId(areaId, userId))
                .thenReturn(Optional.of(Area.rehydrate(areaId, "Area", "Loc", 10.0, userId, Instant.now())));
        when(sensorPlanPort.save(any(SensorPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var output = useCase.updateStatus(
                new UpdateSensorPlanStatusCommand(planId, "REJECTED", "nao aprovado"),
                userId,
                Role.OPERADOR
        );

        assertEquals(SensorPlanStatus.REJECTED, output.status());
        verify(sensorPort, never()).save(any(Sensor.class));
    }

    private SensorPlan buildPlan(UUID planId, SensorPlanStatus status, UUID specialistId) {
        return new SensorPlan(
                planId,
                areaId,
                userId,
                specialistId,
                status,
                null,
                Instant.now(),
                null
        );
    }
}

