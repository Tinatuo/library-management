package com.example.library.member.mapper;

import com.example.library.member.dto.MemberRequestDto;
import com.example.library.member.dto.MemberResponseDto;
import com.example.library.member.entity.Member;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MemberMapper {

    public Member toEntity(MemberRequestDto dto) {
        LocalDate startDate = LocalDate.now();
        return Member.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .membershipStartDate(startDate)
                .membershipExpiryDate(startDate.plusYears(1))
                .build();
    }

    public void updateEntityFromDto(MemberRequestDto dto, Member member) {
        member.setFullName(dto.getFullName());
        member.setEmail(dto.getEmail());
        member.setPhoneNumber(dto.getPhoneNumber());
    }

    public MemberResponseDto toResponseDto(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .membershipStartDate(member.getMembershipStartDate())
                .membershipExpiryDate(member.getMembershipExpiryDate())
                .membershipExpired(member.isMembershipExpired())
                .build();
    }
}
