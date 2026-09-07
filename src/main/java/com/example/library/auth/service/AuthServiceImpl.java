package com.example.library.auth.service;

import com.example.library.auth.dto.AuthResponseDto;
import com.example.library.auth.dto.LoginRequestDto;
import com.example.library.auth.dto.RefreshTokenRequestDto;
import com.example.library.auth.entity.RefreshToken;
import com.example.library.auth.entity.Role;
import com.example.library.auth.entity.User;
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
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UserService userService,
                           RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public AuthResponseDto login(LoginRequestDto requestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        User user = userService.getUserEntityByUsername(principal.getUsername());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        String role = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return AuthResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .userId(principal.getUserId())
                .username(principal.getUsername())
                .role(com.example.library.auth.entity.Role.valueOf(role))
                .memberId(principal.getMemberId())
                .expiresInMs(jwtService.getExpirationMs())
                .build();
    }

    @Override
    public AuthResponseDto refreshToken(RefreshTokenRequestDto requestDto) {
        RefreshToken oldRefreshToken = refreshTokenService.verifyAndGet(requestDto.getRefreshToken());
        User user=oldRefreshToken.getUser();

        oldRefreshToken.setRevoked(true);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        UserPrincipal principal = new UserPrincipal(user);
        String newToken = jwtService.generateToken(principal);

        return buildAuthResponse(principal,newToken,newRefreshToken.getToken());
    }


    @Override
    public void logout(RefreshTokenRequestDto requestDto) {
        refreshTokenService.revokeByTokenIfPresent(requestDto.getRefreshToken());
    }

    private AuthResponseDto buildAuthResponse(UserPrincipal principal, String accessToken, String refreshToken) {
        String role = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return AuthResponseDto.builder()
                .token(accessToken)
                .tokenType("Bearer")
                .refreshToken(refreshToken)
                .userId(principal.getUserId())
                .username(principal.getUsername())
                .role(Role.valueOf(role))
                .memberId(principal.getMemberId())
                .expiresInMs(jwtService.getExpirationMs())
                .build();
    }
}
