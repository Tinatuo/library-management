package com.example.library.member.service;

import com.example.library.common.dto.PageResponseDto;
import com.example.library.member.dto.MemberRequestDto;
import com.example.library.member.dto.MemberResponseDto;
import com.example.library.member.entity.Member;

import java.util.List;

public interface MemberService {

    MemberResponseDto createMember(MemberRequestDto requestDto);

    MemberResponseDto getMemberById(Long id);

    Member getMemberEntityById(Long id);

    PageResponseDto<MemberResponseDto> getAllMembers(int page, int size);

    MemberResponseDto updateMember(Long id, MemberRequestDto requestDto);

    boolean existsById(Long id);

}
