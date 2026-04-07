package com.agrotech.system.infrastructure.config;

import com.agrotech.system.application.port.in.AuthUseCase;
import com.agrotech.system.application.port.in.area.CreateAreaUseCase;
import com.agrotech.system.application.port.in.area.DeleteAreaUseCase;
import com.agrotech.system.application.port.in.area.GetAreaByIdUseCase;
import com.agrotech.system.application.port.in.area.ListMyAreasUseCase;
import com.agrotech.system.application.port.in.area.UpdateAreaUseCase;
import com.agrotech.system.application.port.out.AreaRepositoryPort;
import com.agrotech.system.application.port.out.AccessTokenPort;
import com.agrotech.system.application.port.out.AuthenticationPort;
import com.agrotech.system.application.port.out.PasswordHashPort;
import com.agrotech.system.application.port.out.UserPort;
import com.agrotech.system.application.usecase.AreaUseCase;
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

    @Bean
    public AreaUseCase areaUseCase(AreaRepositoryPort areaRepositoryPort) {
        return new AreaUseCase(areaRepositoryPort);
    }

    @Bean
    public CreateAreaUseCase createAreaUseCase(AreaUseCase areaUseCase) {
        return areaUseCase;
    }

    @Bean
    public UpdateAreaUseCase updateAreaUseCase(AreaUseCase areaUseCase) {
        return areaUseCase;
    }

    @Bean
    public DeleteAreaUseCase deleteAreaUseCase(AreaUseCase areaUseCase) {
        return areaUseCase;
    }

    @Bean
    public GetAreaByIdUseCase getAreaByIdUseCase(AreaUseCase areaUseCase) {
        return areaUseCase;
    }

    @Bean
    public ListMyAreasUseCase listMyAreasUseCase(AreaUseCase areaUseCase) {
        return areaUseCase;
    }
}
