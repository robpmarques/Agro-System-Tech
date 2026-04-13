package com.agrotech.system.infrastructure.config;

import com.agrotech.system.application.port.in.AuthUseCase;
import com.agrotech.system.application.port.in.SensorReadingSimulationUseCase;
import com.agrotech.system.application.port.in.SensorReadingUseCase;
import com.agrotech.system.application.port.in.alert.ListAlertsUseCase;
import com.agrotech.system.application.port.in.alert.ResolveAlertUseCase;
import com.agrotech.system.application.port.in.area.CreateAreaUseCase;
import com.agrotech.system.application.port.in.area.DeleteAreaUseCase;
import com.agrotech.system.application.port.in.area.GetAreaByIdUseCase;
import com.agrotech.system.application.port.in.area.ListMyAreasUseCase;
import com.agrotech.system.application.port.in.area.UpdateAreaUseCase;
import com.agrotech.system.application.port.in.sensor.CreateSensorUseCase;
import com.agrotech.system.application.port.in.sensor.DeleteSensorUseCase;
import com.agrotech.system.application.port.in.sensor.GetSensorByIdUseCase;
import com.agrotech.system.application.port.in.sensor.ListSensorsUseCase;
import com.agrotech.system.application.port.in.sensor.UpdateSensorActivationUseCase;
import com.agrotech.system.application.port.in.sensor.UpdateSensorUseCase;
import com.agrotech.system.application.port.in.sensorplan.AssignSpecialistUseCase;
import com.agrotech.system.application.port.in.sensorplan.CreatePlannedSensorUseCase;
import com.agrotech.system.application.port.in.sensorplan.CreateSensorPlanUseCase;
import com.agrotech.system.application.port.in.sensorplan.ListPlannedSensorsUseCase;
import com.agrotech.system.application.port.in.sensorplan.ListSensorPlansUseCase;
import com.agrotech.system.application.port.in.sensorplan.UpdateSensorPlanStatusUseCase;
import com.agrotech.system.application.port.in.rule.CreateRuleUseCase;
import com.agrotech.system.application.port.in.rule.DeleteRuleUseCase;
import com.agrotech.system.application.port.in.rule.ListRulesBySensorUseCase;
import com.agrotech.system.application.port.in.rule.UpdateRuleUseCase;
import com.agrotech.system.application.port.out.AccessTokenPort;
import com.agrotech.system.application.port.out.AlertPort;
import com.agrotech.system.application.port.out.AlertRealtimePort;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.AuthenticationPort;
import com.agrotech.system.application.port.out.PlannedSensorPort;
import com.agrotech.system.application.port.out.PasswordHashPort;
import com.agrotech.system.application.port.out.RulePort;
import com.agrotech.system.application.port.out.SensorPlanPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.application.port.out.SensorReadingPort;
import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.application.usecase.AlertEvaluationService;
import com.agrotech.system.application.usecase.AlertUseCase;
import com.agrotech.system.application.usecase.AreaUseCase;
import com.agrotech.system.application.usecase.AuthUseCaseImpl;
import com.agrotech.system.application.usecase.SensorReadingImpl;
import com.agrotech.system.application.usecase.SensorReadingSimulationUseCaseImpl;
import com.agrotech.system.application.usecase.SensorUseCase;
import com.agrotech.system.application.usecase.RuleUseCase;
import com.agrotech.system.application.usecase.SensorPlanningUseCase;
import com.agrotech.system.domain.service.UserDomainService;
import com.agrotech.system.infrastructure.websocket.AlertWebSocketPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class ApplicationLayerConfig {

    @Bean
    public UserDomainService userDomainService() {
        return new UserDomainService();
    }

    @Bean
    public AuthUseCase authUseCase(
            UserPort userPort,
            PasswordHashPort passwordHashPort,
            AuthenticationPort authenticationPort,
            AccessTokenPort accessTokenPort,
            UserDomainService userDomainService
    ) {
        return new AuthUseCaseImpl(
                userPort,
                passwordHashPort,
                authenticationPort,
                accessTokenPort,
                userDomainService
        );
    }

    @Bean
    public AreaUseCase areaUseCase(AreaRepositoryPort areaRepositoryPort) {
        return new AreaUseCase(areaRepositoryPort);
    }

    @Bean
    public CreateAreaUseCase createAreaUseCase(AreaUseCase areaUseCase) {
        return areaUseCase;
    }

    @Bean
    public UpdateAreaUseCase updateAreaUseCase(AreaUseCase areaUseCase) {
        return areaUseCase;
    }

    @Bean
    public DeleteAreaUseCase deleteAreaUseCase(AreaUseCase areaUseCase) {
        return areaUseCase;
    }

    @Bean
    public GetAreaByIdUseCase getAreaByIdUseCase(AreaUseCase areaUseCase) {
        return areaUseCase;
    }

    @Bean
    public ListMyAreasUseCase listMyAreasUseCase(AreaUseCase areaUseCase) {
        return areaUseCase;
    }

    @Bean
    public SensorReadingUseCase sensorReadingUseCase(
            SensorReadingPort sensorReadingPort,
            SensorPort sensorPort,
            AreaRepositoryPort areaRepositoryPort,
            AlertEvaluationService alertEvaluationService
    ) {
        return new SensorReadingImpl(sensorReadingPort, sensorPort, areaRepositoryPort, alertEvaluationService);
    }

    @Bean
    public SensorReadingSimulationUseCase sensorReadingSimulationUseCase(
            SensorPort sensorPort,
            SensorReadingPort sensorReadingPort,
            AlertEvaluationService alertEvaluationService
    ) {
        return new SensorReadingSimulationUseCaseImpl(sensorPort, sensorReadingPort, alertEvaluationService);
    }

    @Bean
    public AlertEvaluationService alertEvaluationService(
            RulePort rulePort,
            AlertPort alertPort,
            AlertRealtimePort alertRealtimePort
    ) {
        return new AlertEvaluationService(rulePort, alertPort, alertRealtimePort);
    }

    @Bean
    @Lazy
    public AlertRealtimePort alertRealtimePort(SimpMessagingTemplate messagingTemplate) {
        return new AlertWebSocketPublisher(messagingTemplate);
    }

    @Bean
    public SensorUseCase sensorUseCase(
            SensorPort sensorPort,
            AreaRepositoryPort areaRepositoryPort
    ) {
        return new SensorUseCase(sensorPort, areaRepositoryPort);
    }

    @Bean
    public CreateSensorUseCase createSensorUseCase(SensorUseCase sensorUseCase) {
        return sensorUseCase;
    }

    @Bean
    public ListSensorsUseCase listSensorsUseCase(SensorUseCase sensorUseCase) {
        return sensorUseCase;
    }

    @Bean
    public GetSensorByIdUseCase getSensorByIdUseCase(SensorUseCase sensorUseCase) {
        return sensorUseCase;
    }

    @Bean
    public UpdateSensorUseCase updateSensorUseCase(SensorUseCase sensorUseCase) {
        return sensorUseCase;
    }

    @Bean
    public UpdateSensorActivationUseCase updateSensorActivationUseCase(SensorUseCase sensorUseCase) {
        return sensorUseCase;
    }

    @Bean
    public DeleteSensorUseCase deleteSensorUseCase(SensorUseCase sensorUseCase) {
        return sensorUseCase;
    }

    @Bean
    public SensorPlanningUseCase sensorPlanningUseCase(
            SensorPlanPort sensorPlanPort,
            PlannedSensorPort plannedSensorPort,
            SensorPort sensorPort,
            AreaRepositoryPort areaRepositoryPort,
            UserPort userPort
    ) {
        return new SensorPlanningUseCase(sensorPlanPort, plannedSensorPort, sensorPort, areaRepositoryPort, userPort);
    }

    @Bean
    public CreateSensorPlanUseCase createSensorPlanUseCase(SensorPlanningUseCase sensorPlanningUseCase) {
        return sensorPlanningUseCase;
    }

    @Bean
    public ListSensorPlansUseCase listSensorPlansUseCase(SensorPlanningUseCase sensorPlanningUseCase) {
        return sensorPlanningUseCase;
    }

    @Bean
    public AssignSpecialistUseCase assignSpecialistUseCase(SensorPlanningUseCase sensorPlanningUseCase) {
        return sensorPlanningUseCase;
    }

    @Bean
    public CreatePlannedSensorUseCase createPlannedSensorUseCase(SensorPlanningUseCase sensorPlanningUseCase) {
        return sensorPlanningUseCase;
    }

    @Bean
    public ListPlannedSensorsUseCase listPlannedSensorsUseCase(SensorPlanningUseCase sensorPlanningUseCase) {
        return sensorPlanningUseCase;
    }

    @Bean
    public UpdateSensorPlanStatusUseCase updateSensorPlanStatusUseCase(SensorPlanningUseCase sensorPlanningUseCase) {
        return sensorPlanningUseCase;
    }

    @Bean
    public RuleUseCase ruleUseCase(
            RulePort rulePort,
            SensorPort sensorPort,
            AreaRepositoryPort areaRepositoryPort
    ) {
        return new RuleUseCase(rulePort, sensorPort, areaRepositoryPort);
    }

    @Bean
    public CreateRuleUseCase createRuleUseCase(RuleUseCase ruleUseCase) {
        return ruleUseCase;
    }

    @Bean
    public ListRulesBySensorUseCase listRulesBySensorUseCase(RuleUseCase ruleUseCase) {
        return ruleUseCase;
    }

    @Bean
    public UpdateRuleUseCase updateRuleUseCase(RuleUseCase ruleUseCase) {
        return (command, currentUserId, currentRole) ->
                ruleUseCase.update(command, currentUserId, currentRole);
    }

    @Bean
    public DeleteRuleUseCase deleteRuleUseCase(RuleUseCase ruleUseCase) {
        return (ruleId, currentUserId, currentRole) ->
                ruleUseCase.delete(ruleId, currentUserId, currentRole);
    }

    @Bean
    public AlertUseCase alertUseCase(
            AlertPort alertPort,
            AlertRealtimePort alertRealtimePort,
            SensorPort sensorPort,
            AreaRepositoryPort areaRepositoryPort
    ) {
        return new AlertUseCase(alertPort, alertRealtimePort, sensorPort, areaRepositoryPort);
    }

    @Bean
    public ListAlertsUseCase listAlertsUseCase(AlertUseCase alertUseCase) {
        return alertUseCase;
    }

    @Bean
    public ResolveAlertUseCase resolveAlertUseCase(AlertUseCase alertUseCase) {
        return alertUseCase;
    }
}
