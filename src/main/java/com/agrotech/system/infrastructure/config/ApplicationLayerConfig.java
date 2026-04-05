package com.agrotech.system.infrastructure.config;

import com.agrotech.system.application.port.in.AuthUseCase;
import com.agrotech.system.application.port.out.AccessTokenPort;
import com.agrotech.system.application.port.out.AuthenticationPort;
import com.agrotech.system.application.port.out.PasswordHashPort;
import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.application.usecase.AuthUseCaseImpl;
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
}
