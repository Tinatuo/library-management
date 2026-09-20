package com.example.library.auth.service;

import com.example.library.auth.dto.RegisterRequestDto;
import com.example.library.auth.dto.StaffRegisterRequestDto;
import com.example.library.auth.dto.UserResponseDto;
import com.example.library.auth.entity.Role;
import com.example.library.auth.entity.User;
import com.example.library.auth.mapper.UserMapper;
import com.example.library.auth.repository.UserRepository;
import com.example.library.common.aop.Audited;
import com.example.library.common.exception.BusinessRuleViolationException;
import com.example.library.common.exception.DuplicateResourceException;
import com.example.library.common.exception.ResourceNotFoundException;
import com.example.library.member.dto.MemberRequestDto;
import com.example.library.member.dto.MemberResponseDto;
import com.example.library.member.service.MemberService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MemberService memberService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, MemberService memberService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.memberService = memberService;
    }

    @Override
    @Audited(action = "USER_REGISTER", details = "username=#{#requestDto.username}")
    public UserResponseDto registerMember(RegisterRequestDto requestDto) {

        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new DuplicateResourceException("This username is already taken");
        }

        MemberRequestDto memberRequestDto = new MemberRequestDto();
        memberRequestDto.setFullName(requestDto.getFullName());
        memberRequestDto.setPhoneNumber(String.valueOf(requestDto.getPhoneNumber()));
        memberRequestDto.setEmail(requestDto.getEmail());
        MemberResponseDto createdMember = memberService.createMember(memberRequestDto);

        User user = User.builder()
                .username(requestDto.getUsername())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(Role.MEMBER)
                .enabled(true)
                .memberId(createdMember.getId())
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);

    }

    @Override
    @Audited(action = "STAFF_REGISTER", details = "username=#{#requestDto.username}")
    public UserResponseDto registerStaff(StaffRegisterRequestDto requestDto) {

        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new DuplicateResourceException("This username is already taken");
        }

        if (requestDto.getRole() == Role.MEMBER) {
            throw new BusinessRuleViolationException(
                    "Use the public registration endpoint to create MEMBER accounts");
        }

        User user =User.builder()
                .username(requestDto.getUsername())
                .password(requestDto.getPassword())
                .role(Role.LIBRARIAN)
                .memberId(null)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User '" + username + "' was not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String username) {
        return userMapper.toResponseDto(getUserEntityByUsername(username));
    }
}

