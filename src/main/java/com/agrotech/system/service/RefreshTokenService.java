package com.agrotech.system.service;

import com.agrotech.system.application.exception.UnauthorizedException;
import com.agrotech.system.application.port.out.RefreshTokenPort;
import com.agrotech.system.model.RefreshToken;
import com.agrotech.system.model.User;
import com.agrotech.system.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService implements RefreshTokenPort {

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Cria um novo refresh token para o usuario.
     * Revoga todos os tokens ativos anteriores (uma sessao ativa por vez).
     */
    @Transactional
    @Override
    public RefreshToken create(User user) {
        refreshTokenRepository.revokeAllActiveByUser(user);
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setExpiresAt(Instant.now().plusMillis(refreshTokenExpirationMs));
        return refreshTokenRepository.save(rt);
    }

    /**
     * Rotaciona o refresh token: valida o atual, revoga e emite um novo.
     * Implementa rotacao com deteccao de reutilizacao.
     */
    @Transactional
    @Override
    public RefreshToken rotate(String tokenStr) {
        RefreshToken old = findAndValidate(tokenStr);
        old.setRevoked(true);

        RefreshToken newRt = new RefreshToken();
        newRt.setToken(UUID.randomUUID().toString());
        newRt.setUser(old.getUser());
        newRt.setExpiresAt(Instant.now().plusMillis(refreshTokenExpirationMs));
        return refreshTokenRepository.save(newRt);
    }

    /**
     * Revoga o refresh token (logout). Operacao idempotente.
     */
    @Transactional
    @Override
    public void revoke(String tokenStr) {
        refreshTokenRepository.findByToken(tokenStr)
                .ifPresent(rt -> rt.setRevoked(true));
    }

    private RefreshToken findAndValidate(String tokenStr) {
        RefreshToken rt = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalido"));
        if (rt.isRevoked()) {
            throw new UnauthorizedException("Refresh token revogado");
        }
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expirado");
        }
        return rt;
    }
}
