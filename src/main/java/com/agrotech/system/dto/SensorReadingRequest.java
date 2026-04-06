package com.agrotech.system.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SensorReadingRequest(
        @NotNull(message = "Sensor ID é obrigatório")
        UUID sensorId,

        @NotNull(message = "Valor da leitura é obrigatório")
        Double value,

        @NotNull(message = "Data e hora da leitura é obrigatória")
        Instant recordedAt,

        Map<String, Object> data
) {
}

