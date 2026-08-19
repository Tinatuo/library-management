package com.example.library.loan.mapper;

import com.example.library.loan.dto.LoanResponseDto;
import com.example.library.loan.entity.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanResponseDto toResponseDto(Loan loan) {
        return LoanResponseDto.builder()
                .id(loan.getId())
                .bookId(loan.getBook().getId())
                .bookTitle(loan.getBook().getTitle())
                .memberId(loan.getMember().getId())
                .memberFullName(loan.getMember().getFullName())
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus())
                .build();
    }
}
