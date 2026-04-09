package com.agrotech.system.infrastructure.scheduler;

import com.agrotech.system.application.port.in.SensorReadingSimulationUseCase;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SensorReadingSchedulerAdapterTest {

    @Test
    void simulateReadingsJob_deveDelegarExecucaoParaUseCase() {
        SensorReadingSimulationUseCase useCase = mock(SensorReadingSimulationUseCase.class);
        SensorReadingSchedulerAdapter scheduler = new SensorReadingSchedulerAdapter(useCase);

        scheduler.simulateReadingsJob();

        verify(useCase, times(1)).simulateReadings();
    }
}

