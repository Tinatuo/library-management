package com.example.library.loan.service;

import com.example.library.common.dto.PageResponseDto;
import com.example.library.loan.dto.LoanRequestDto;
import com.example.library.loan.dto.LoanResponseDto;

import java.util.List;

public interface LoanService {

    LoanResponseDto borrowBook(LoanRequestDto requestDto);

    LoanResponseDto returnBook(Long loanId);

    LoanResponseDto renewLoan(Long loanId);

    LoanResponseDto getLoanById(Long id);

    PageResponseDto<LoanResponseDto> getAllLoans(int page, int size);

    PageResponseDto<LoanResponseDto> getLoansByMember(Long memberId,int page, int size);
}
