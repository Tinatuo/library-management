package com.example.library.member.service;

import com.example.library.common.exception.DuplicateResourceException;
import com.example.library.common.exception.ResourceNotFoundException;
import com.example.library.member.dto.MemberRequestDto;
import com.example.library.member.dto.MemberResponseDto;
import com.example.library.member.entity.Member;
import com.example.library.member.mapper.MemberMapper;
import com.example.library.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public MemberServiceImpl(MemberRepository memberRepository, MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
    }

    @Override
    public MemberResponseDto createMember(MemberRequestDto requestDto) {
        if (memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateResourceException("A member with this email is already registered");
        }
        Member member = memberMapper.toEntity(requestDto);
        Member savedMember = memberRepository.save(member);
        return memberMapper.toResponseDto(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponseDto getMemberById(Long id) {
        Member member = findMemberOrThrow(id);
        return memberMapper.toResponseDto(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponseDto> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(member -> memberMapper.toResponseDto(member))
                .toList();
    }

    @Override
    public MemberResponseDto updateMember(Long id, MemberRequestDto requestDto) {
        Member member = findMemberOrThrow(id);

        memberRepository.findByEmail(requestDto.getEmail())
                .filter(existingMember -> !existingMember.getId().equals(id))
                .ifPresent(existingMember -> {
                    throw new DuplicateResourceException("A member with this email is already registered");
                });

        memberMapper.updateEntityFromDto(requestDto, member);
        Member updatedMember = memberRepository.save(member);
        return memberMapper.toResponseDto(updatedMember);
    }

    private Member findMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + id + " was not found"));
    }
}
