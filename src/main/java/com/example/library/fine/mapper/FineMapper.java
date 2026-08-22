package com.example.library.fine.mapper;

import com.example.library.fine.dto.FineResponseDto;
import com.example.library.fine.entity.Fine;
import org.springframework.stereotype.Component;

@Component
public class FineMapper {

    public FineResponseDto toResponseDto(Fine fine) {
        return FineResponseDto.builder()
                .id(fine.getId())
                .loanId(fine.getLoan().getId())
                .bookId(fine.getLoan().getBook().getId())
                .bookTitle(fine.getLoan().getBook().getTitle())
                .memberId(fine.getMember().getId())
                .memberFullName(fine.getMember().getFullName())
                .daysLate(fine.getDaysLate())
                .amount(fine.getAmount())
                .status(fine.getStatus())
                .issuedDate(fine.getIssuedDate())
                .paidDate(fine.getPaidDate())
                .build();
    }
}
