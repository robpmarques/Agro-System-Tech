package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.in.sensorplan.AssignSpecialistCommand;
import com.agrotech.system.application.port.in.sensorplan.AssignSpecialistUseCase;
import com.agrotech.system.application.port.in.sensorplan.CreatePlannedSensorCommand;
import com.agrotech.system.application.port.in.sensorplan.CreatePlannedSensorUseCase;
import com.agrotech.system.application.port.in.sensorplan.CreateSensorPlanCommand;
import com.agrotech.system.application.port.in.sensorplan.CreateSensorPlanUseCase;
import com.agrotech.system.application.port.in.sensorplan.ListPlannedSensorsUseCase;
import com.agrotech.system.application.port.in.sensorplan.ListSensorPlansUseCase;
import com.agrotech.system.application.port.in.sensorplan.PlannedSensorOutput;
import com.agrotech.system.application.port.in.sensorplan.SensorPlanOutput;
import com.agrotech.system.application.port.in.sensorplan.UpdateSensorPlanStatusCommand;
import com.agrotech.system.application.port.in.sensorplan.UpdateSensorPlanStatusUseCase;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.PlannedSensorPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.application.port.out.SensorPlanPort;
import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.domain.exception.ForbiddenException;
import com.agrotech.system.domain.exception.NotFoundException;
import com.agrotech.system.domain.model.PlannedSensor;
import com.agrotech.system.domain.model.Role;
import com.agrotech.system.domain.model.SensorPlan;
import com.agrotech.system.domain.model.SensorPlanStatus;
import com.agrotech.system.domain.model.SensorPosition;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.domain.model.SensorType;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SensorPlanningUseCase implements
        CreateSensorPlanUseCase,
        ListSensorPlansUseCase,
        AssignSpecialistUseCase,
        CreatePlannedSensorUseCase,
        ListPlannedSensorsUseCase,
        UpdateSensorPlanStatusUseCase {

    private final SensorPlanPort sensorPlanPort;
    private final PlannedSensorPort plannedSensorPort;
    private final SensorPort sensorPort;
    private final AreaRepositoryPort areaRepositoryPort;
    private final UserPort userPort;

    public SensorPlanningUseCase(
            SensorPlanPort sensorPlanPort,
            PlannedSensorPort plannedSensorPort,
            SensorPort sensorPort,
            AreaRepositoryPort areaRepositoryPort,
            UserPort userPort
    ) {
        this.sensorPlanPort = sensorPlanPort;
        this.plannedSensorPort = plannedSensorPort;
        this.sensorPort = sensorPort;
        this.areaRepositoryPort = areaRepositoryPort;
        this.userPort = userPort;
    }

    @Override
    public SensorPlanOutput create(CreateSensorPlanCommand command, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        UUID areaId = requireUuid(command.areaId(), "Area id is required");
        ensureVisibleArea(areaId, currentUserId, currentRole);

        SensorPlan plan = new SensorPlan();
        plan.setAreaId(areaId);
        plan.setRequestedBy(currentUserId);
        plan.setStatus(SensorPlanStatus.PENDING);
        plan.setNotes(normalizeOptional(command.notes()));
        plan.setCreatedAt(Instant.now());

        return toOutput(sensorPlanPort.save(plan));
    }

    @Override
    public List<SensorPlanOutput> list(UUID areaId, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        UUID requiredAreaId = requireUuid(areaId, "Area id is required");
        ensureVisibleArea(requiredAreaId, currentUserId, currentRole);

        return sensorPlanPort.findAllByAreaId(requiredAreaId).stream()
                .map(this::toOutput)
                .toList();
    }

    @Override
    public SensorPlanOutput assignSpecialist(AssignSpecialistCommand command, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        SensorPlan plan = findVisiblePlan(command.planId(), currentUserId, currentRole);

        UUID specialistId = requireUuid(command.specialistId(), "Specialist id is required");
        if (!userPort.existsById(specialistId)) {
            throw new NotFoundException("Usuario responsavel nao encontrado");
        }
        plan.setSpecialistId(specialistId);
        plan.setStatus(SensorPlanStatus.IN_PROGRESS);

        return toOutput(sensorPlanPort.save(plan));
    }

    @Override
    public PlannedSensorOutput createPlannedSensor(CreatePlannedSensorCommand command, UUID currentUserId, Role currentRole) {
        SensorPlan plan = findVisiblePlan(command.planId(), currentUserId, currentRole);
        ensureOperatorOrAdmin(currentRole);

        PlannedSensor plannedSensor = new PlannedSensor();
        plannedSensor.setPlanId(plan.getId());
        plannedSensor.setName(normalizeRequired(command.name(), "Planned sensor name is required"));
        plannedSensor.setType(parseType(command.type()));
        plannedSensor.setPosition(parsePosition(command.position()));
        plannedSensor.setCreatedAt(Instant.now());

        return toOutput(plannedSensorPort.save(plannedSensor));
    }

    @Override
    public List<PlannedSensorOutput> listPlannedSensors(UUID planId, UUID currentUserId, Role currentRole) {
        SensorPlan plan = findVisiblePlan(planId, currentUserId, currentRole);
        return plannedSensorPort.findAllByPlanId(plan.getId()).stream()
                .map(this::toOutput)
                .toList();
    }

    @Override
    public SensorPlanOutput updateStatus(UpdateSensorPlanStatusCommand command, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        SensorPlan plan = findVisiblePlan(command.planId(), currentUserId, currentRole);

        SensorPlanStatus targetStatus = parsePlanStatus(command.status());
        if (targetStatus != SensorPlanStatus.APPROVED && targetStatus != SensorPlanStatus.REJECTED) {
            throw new IllegalArgumentException("Apenas status APPROVED ou REJECTED e permitido");
        }
        if (plan.getStatus() == SensorPlanStatus.APPROVED || plan.getStatus() == SensorPlanStatus.REJECTED) {
            throw new IllegalArgumentException("Plano ja finalizado");
        }

        if (targetStatus == SensorPlanStatus.APPROVED) {
            approvePlan(plan);
        }

        plan.setStatus(targetStatus);
        plan.setNotes(normalizeOptional(command.notes()));
        plan.setReviewedAt(Instant.now());

        return toOutput(sensorPlanPort.save(plan));
    }

    private void approvePlan(SensorPlan plan) {
        List<PlannedSensor> plannedSensors = plannedSensorPort.findAllByPlanId(plan.getId());
        if (plannedSensors.isEmpty()) {
            throw new IllegalArgumentException("Plano sem sensores planejados nao pode ser aprovado");
        }

        for (PlannedSensor planned : plannedSensors) {
            Sensor sensor = new Sensor();
            sensor.setName(planned.getName());
            sensor.setType(planned.getType().name());
            sensor.setPosition(planned.getPosition().name());
            sensor.setAreaId(plan.getAreaId());
            sensor.setActive(true);
            sensorPort.save(sensor);
        }
    }

    private SensorPlan findVisiblePlan(UUID planId, UUID currentUserId, Role currentRole) {
        ensureOperatorOrAdmin(currentRole);
        UUID requiredPlanId = requireUuid(planId, "Plan id is required");

        SensorPlan plan = sensorPlanPort.findById(requiredPlanId)
                .orElseThrow(() -> new NotFoundException("Plano de sensores nao encontrado"));

        ensureVisibleArea(plan.getAreaId(), currentUserId, currentRole);

        return plan;
    }

    private void ensureVisibleArea(UUID areaId, UUID currentUserId, Role currentRole) {
        if (currentRole == Role.ADMIN) {
            areaRepositoryPort.findById(areaId)
                    .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
            return;
        }

        areaRepositoryPort.findByIdAndUserId(areaId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Area nao encontrada"));
    }

    private void ensureOperatorOrAdmin(Role role) {
        if (role != Role.OPERADOR && role != Role.ADMIN) {
            throw new ForbiddenException("Perfil sem permissao para gerenciar planos de sensores");
        }
    }


    private UUID requireUuid(UUID value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private SensorType parseType(String value) {
        try {
            return SensorType.valueOf(normalizeRequired(value, "Planned sensor type is required").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Planned sensor type is invalid");
        }
    }

    private SensorPosition parsePosition(String value) {
        try {
            return SensorPosition.valueOf(normalizeRequired(value, "Planned sensor position is required").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Planned sensor position is invalid");
        }
    }

    private SensorPlanStatus parsePlanStatus(String value) {
        try {
            return SensorPlanStatus.valueOf(normalizeRequired(value, "Plan status is required").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Plan status is invalid");
        }
    }

    private SensorPlanOutput toOutput(SensorPlan plan) {
        return new SensorPlanOutput(
                plan.getId(),
                plan.getAreaId(),
                plan.getRequestedBy(),
                plan.getSpecialistId(),
                plan.getStatus(),
                plan.getNotes(),
                plan.getCreatedAt(),
                plan.getReviewedAt()
        );
    }

    private PlannedSensorOutput toOutput(PlannedSensor plannedSensor) {
        return new PlannedSensorOutput(
                plannedSensor.getId(),
                plannedSensor.getPlanId(),
                plannedSensor.getName(),
                plannedSensor.getType(),
                plannedSensor.getPosition(),
                plannedSensor.getCreatedAt()
        );
    }
}

