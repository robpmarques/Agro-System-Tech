package com.agrotech.system.application.port.in;

import com.agrotech.system.dto.SensorReadingRequest;
import com.agrotech.system.dto.SensorReadingResponse;

public interface SensorReadingUseCase {
    /**
     * Registra uma nova leitura de sensor.
     *
     * @param request dados da leitura
     * @return resposta com a leitura salva
     */
    SensorReadingResponse recordReading(SensorReadingRequest request);
}
