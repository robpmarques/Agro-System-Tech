package com.agrotech.system.application.port.out;

public interface PasswordHashPort {
    String hash(String rawPassword);
}
