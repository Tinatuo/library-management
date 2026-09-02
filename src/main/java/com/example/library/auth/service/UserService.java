package com.example.library.auth.service;

import com.example.library.auth.dto.RegisterRequestDto;
import com.example.library.auth.dto.StaffRegisterRequestDto;
import com.example.library.auth.dto.UserResponseDto;
import com.example.library.auth.entity.User;

public interface UserService {


    UserResponseDto registerMember(RegisterRequestDto requestDto);

    UserResponseDto registerStaff(StaffRegisterRequestDto requestDto);

    User getUserEntityByUsername(String username);

    UserResponseDto getCurrentUser(String username);
}
