package com.example.library.member.service;

import com.example.library.member.dto.MemberRequestDto;
import com.example.library.member.dto.MemberResponseDto;
import com.example.library.member.entity.Member;

import java.util.List;

public interface MemberService {

    MemberResponseDto createMember(MemberRequestDto requestDto);

    MemberResponseDto getMemberById(Long id);

    Member getMemberEntityById(Long id);

    List<MemberResponseDto> getAllMembers();

    MemberResponseDto updateMember(Long id, MemberRequestDto requestDto);

    boolean existsById(Long id);

}
