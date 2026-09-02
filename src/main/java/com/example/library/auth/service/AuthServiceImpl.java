package com.example.library.auth.service;

import com.example.library.auth.dto.AuthResponseDto;
import com.example.library.auth.dto.LoginRequestDto;
import com.example.library.auth.security.JwtService;
import com.example.library.auth.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponseDto login(LoginRequestDto requestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        String role = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return AuthResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(principal.getUserId())
                .username(principal.getUsername())
                .role(com.example.library.auth.entity.Role.valueOf(role))
                .memberId(principal.getMemberId())
                .expiresInMs(jwtService.getExpirationMs())
                .build();
    }
}
