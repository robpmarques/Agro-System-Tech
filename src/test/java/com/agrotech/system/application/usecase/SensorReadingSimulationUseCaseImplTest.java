package com.agrotech.system.application.usecase;

import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.application.port.out.SensorReadingPort;
import com.agrotech.system.domain.model.Sensor;
import com.agrotech.system.domain.model.SensorReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorReadingSimulationUseCaseImplTest {

    @Mock
    private SensorPort sensorPort;

    @Mock
    private SensorReadingPort sensorReadingPort;

    @Mock
    private AlertEvaluationService alertEvaluationService;

    private SensorReadingSimulationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SensorReadingSimulationUseCaseImpl(sensorPort, sensorReadingPort, alertEvaluationService);
    }

    @Test
    void simulateReadings_deveSalvarUmaLeituraParaCadaSensorAtivoComFaixaCorreta() {
        Sensor temperature = createSensor("TEMPERATURE");
        Sensor soilHumidity = createSensor("SOIL_HUMIDITY");
        Sensor airHumidity = createSensor("AIR_HUMIDITY");
        Sensor luminosity = createSensor("LUMINOSITY");

        when(sensorPort.findAllActive()).thenReturn(List.of(temperature, soilHumidity, airHumidity, luminosity));
        when(sensorReadingPort.save(any(SensorReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.simulateReadings();

        ArgumentCaptor<SensorReading> captor = ArgumentCaptor.forClass(SensorReading.class);
        verify(sensorReadingPort, times(4)).save(captor.capture());
        verify(alertEvaluationService, times(4)).evaluateReading(any(SensorReading.class));

        Map<UUID, BiConsumer<Double, String>> assertionsBySensorId = Map.of(
                temperature.getId(), this::assertTemperature,
                soilHumidity.getId(), this::assertSoilHumidity,
                airHumidity.getId(), this::assertAirHumidity,
                luminosity.getId(), this::assertLuminosity
        );

        for (SensorReading reading : captor.getAllValues()) {
            assertNotNull(reading.getRecordedAt());
            BiConsumer<Double, String> assertion = assertionsBySensorId.get(reading.getSensorId());
            assertion.accept(reading.getValue(), reading.getSensorId().toString());
        }
    }

    @Test
    void simulateReadings_semSensoresAtivosNaoDeveSalvarLeituras() {
        when(sensorPort.findAllActive()).thenReturn(List.of());

        useCase.simulateReadings();

        verify(sensorReadingPort, never()).save(any(SensorReading.class));
        verify(alertEvaluationService, never()).evaluateReading(any(SensorReading.class));
    }

    private Sensor createSensor(String type) {
        return new Sensor(UUID.randomUUID(), "Sensor", type, "P1", UUID.randomUUID(), true, Instant.now());
    }

    private void assertTemperature(Double value, String sensorId) {
        assertRange(value, 10.0, 40.0, sensorId);
    }

    private void assertSoilHumidity(Double value, String sensorId) {
        assertRange(value, 20.0, 90.0, sensorId);
    }

    private void assertAirHumidity(Double value, String sensorId) {
        assertRange(value, 30.0, 80.0, sensorId);
    }

    private void assertLuminosity(Double value, String sensorId) {
        assertRange(value, 100.0, 1000.0, sensorId);
    }

    private void assertRange(Double value, double minInclusive, double maxExclusive, String sensorId) {
        org.junit.jupiter.api.Assertions.assertTrue(
                value >= minInclusive && value < maxExclusive,
                "Valor fora da faixa para sensor " + sensorId + ": " + value
        );
    }
}

