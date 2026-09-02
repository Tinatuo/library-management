package com.example.library.auth.mapper;

import com.example.library.auth.dto.UserResponseDto;
import com.example.library.auth.entity.User;

public class UserMapper {

    public UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .memberId(user.getMemberId())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }
}
