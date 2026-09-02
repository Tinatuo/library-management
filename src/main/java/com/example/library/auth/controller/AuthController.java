package com.example.library.auth.controller;

import com.example.library.auth.dto.*;
import com.example.library.auth.service.AuthService;
import com.example.library.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto requestDto) {
        UserResponseDto created = userService.registerMember(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PostMapping("/register-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> registerStaff(@Valid @RequestBody StaffRegisterRequestDto requestDto) {
        UserResponseDto created = userService.registerStaff(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        return ResponseEntity.ok(authService.login(requestDto));
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication.getName()));
    }
}
