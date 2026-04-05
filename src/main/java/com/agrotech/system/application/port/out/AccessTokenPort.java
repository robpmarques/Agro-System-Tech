package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.User;

public interface AccessTokenPort {
    String generateAccessToken(User user);
}
