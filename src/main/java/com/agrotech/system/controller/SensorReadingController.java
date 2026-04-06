package com.agrotech.system.controller;

import com.agrotech.system.application.port.in.SensorReadingUseCase;
import com.agrotech.system.dto.SensorReadingRequest;
import com.agrotech.system.dto.SensorReadingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/readings")
public class SensorReadingController {

    private final SensorReadingUseCase sensorReadingUseCase;

    public SensorReadingController(SensorReadingUseCase sensorReadingUseCase) {
        this.sensorReadingUseCase = sensorReadingUseCase;
    }

    @PostMapping
    public ResponseEntity<SensorReadingResponse> recordReading(
            @Valid @RequestBody SensorReadingRequest request
    ) {
        SensorReadingResponse response = sensorReadingUseCase.recordReading(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

