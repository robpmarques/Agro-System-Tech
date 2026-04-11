package com.agrotech.system.infrastructure.scheduler;

import com.agrotech.system.application.port.in.SensorReadingSimulationUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingSchedulerAdapter {

    private final SensorReadingSimulationUseCase sensorReadingSimulationUseCase;

    public SensorReadingSchedulerAdapter(SensorReadingSimulationUseCase sensorReadingSimulationUseCase) {
        this.sensorReadingSimulationUseCase = sensorReadingSimulationUseCase;
    }

    @Scheduled(fixedDelayString = "${app.sensor-reading-scheduler.fixed-delay-ms:30000}")
    public void simulateReadingsJob() {
        sensorReadingSimulationUseCase.simulateReadings();
    }
}

