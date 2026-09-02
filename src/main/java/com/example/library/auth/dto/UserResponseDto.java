package com.example.library.auth.dto;

import com.example.library.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String username;
    private Role role;
    private Long memberId;
    private boolean enabled;
}
