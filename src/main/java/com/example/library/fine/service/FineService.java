package com.example.library.fine.service;

import com.example.library.common.dto.PageResponseDto;
import com.example.library.fine.dto.FineResponseDto;
import com.example.library.fine.dto.FineSummaryDto;
import com.example.library.loan.entity.Loan;

import java.util.List;

public interface FineService {

    FineResponseDto issueFine(Loan loan, int daysLate);

    FineResponseDto payFine(Long fineId);

    PageResponseDto<FineResponseDto> getFinesByMember(Long memberId,int page,int size);

    FineSummaryDto getUnpaidSummaryByMember(Long memberId);
}
