package com.example.library.auth.service;

import com.example.library.auth.dto.AuthResponseDto;
import com.example.library.auth.dto.LoginRequestDto;
import com.example.library.auth.dto.RefreshTokenRequestDto;
import org.springframework.stereotype.Service;


public interface AuthService {

    AuthResponseDto login(LoginRequestDto requestDto);


    AuthResponseDto refreshToken(RefreshTokenRequestDto requestDto);


    void logout(RefreshTokenRequestDto requestDto);
}
