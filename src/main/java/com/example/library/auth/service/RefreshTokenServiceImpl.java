package com.example.library.auth.service;

import com.example.library.auth.entity.RefreshToken;
import com.example.library.auth.entity.User;
import com.example.library.auth.repository.RefreshTokenRepository;
import com.example.library.auth.repository.RefreshTokenRepository;
import com.example.library.common.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                   @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }


    @Override
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)))
                .revoked(false)
                .build();
        System.out.println("Saving refresh token: " + refreshToken.getToken());
        return refreshTokenRepository.save(refreshToken);
    }


    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyAndGet(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid"));

        if (refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked, please log in again");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token has expired, please log in again");
        }

        return refreshToken;
    }


    @Override
    public void revoke(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeByTokenIfPresent(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(this::revoke);
    }
}
