package com.agrotech.system.application.port.out;

import com.agrotech.system.model.RefreshToken;
import com.agrotech.system.model.User;

public interface RefreshTokenPort {
    RefreshToken create(User user);
    RefreshToken rotate(String token);
    void revoke(String token);
}
