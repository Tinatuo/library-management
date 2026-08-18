package com.example.library.loan.service;

import com.example.library.loan.dto.LoanRequestDto;
import com.example.library.loan.dto.LoanResponseDto;

import java.util.List;

public interface LoanService {

    LoanResponseDto borrowBook(LoanRequestDto requestDto);

    LoanResponseDto returnBook(Long loanId);

    LoanResponseDto getLoanById(Long id);

    List<LoanResponseDto> getAllLoans();

    List<LoanResponseDto> getLoansByMember(Long memberId);
}
