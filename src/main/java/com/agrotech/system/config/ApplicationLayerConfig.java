package com.agrotech.system.config;

import com.agrotech.system.application.port.in.AuthUseCase;
import com.agrotech.system.application.port.out.AccessTokenPort;
import com.agrotech.system.application.port.out.AuthenticationPort;
import com.agrotech.system.application.port.out.PasswordHashPort;
import com.agrotech.system.application.port.out.RefreshTokenPort;
import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.application.service.AuthApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationLayerConfig {

    @Bean
    public AuthUseCase authUseCase(
            UserPort userPort,
            PasswordHashPort passwordHashPort,
            AuthenticationPort authenticationPort,
            AccessTokenPort accessTokenPort,
            RefreshTokenPort refreshTokenPort
    ) {
        return new AuthApplicationService(
                userPort,
                passwordHashPort,
                authenticationPort,
                accessTokenPort,
                refreshTokenPort
        );
    }
}
