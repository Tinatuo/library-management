package com.example.library.auth.service;

import com.example.library.auth.dto.AuthResponseDto;
import com.example.library.auth.dto.LoginRequestDto;

public interface AuthService {

    AuthResponseDto login(LoginRequestDto requestDto);
}
