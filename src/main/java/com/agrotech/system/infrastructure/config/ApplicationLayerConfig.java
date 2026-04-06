package com.agrotech.system.infrastructure.config;

import com.agrotech.system.application.port.in.AuthUseCase;
import com.agrotech.system.application.port.in.SensorReadingUseCase;
import com.agrotech.system.application.port.out.AccessTokenPort;
import com.agrotech.system.application.port.out.AuthenticationPort;
import com.agrotech.system.application.port.out.PasswordHashPort;
import com.agrotech.system.application.port.out.SensorPort;
import com.agrotech.system.application.port.out.SensorReadingPort;
import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.application.usecase.AuthUseCaseImpl;
import com.agrotech.system.application.usecase.SensorReadingImpl;
import com.agrotech.system.domain.service.UserDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public SensorReadingUseCase sensorReadingUseCase(
            SensorReadingPort sensorReadingPort,
            SensorPort sensorPort
    ) {
        return new SensorReadingImpl(sensorReadingPort, sensorPort);
    }
}
