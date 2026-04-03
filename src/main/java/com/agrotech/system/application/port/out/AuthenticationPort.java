package com.agrotech.system.application.port.out;

public interface AuthenticationPort {
    void authenticate(String email, String password);
}
