package com.agrotech.system.application.port.in;

public interface SensorReadingSimulationUseCase {
    /**
     * Gera e persiste leituras simuladas para sensores ativos.
     */
    void simulateReadings();
}

