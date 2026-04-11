package com.agrotech.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgroTechSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgroTechSystemApplication.class, args);
    }
}

