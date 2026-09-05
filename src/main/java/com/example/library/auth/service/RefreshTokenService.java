package com.example.library.auth.service;

import com.example.library.auth.entity.RefreshToken;
import com.example.library.auth.entity.User;

public interface RefreshTokenService {


    RefreshToken createRefreshToken(User user);


    RefreshToken verifyAndGet(String token);


    void revoke(RefreshToken refreshToken);


    void revokeByTokenIfPresent(String token);
}
