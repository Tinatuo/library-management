package com.example.library.fine.service;

import com.example.library.common.exception.BusinessRuleViolationException;
import com.example.library.common.exception.ResourceNotFoundException;
import com.example.library.fine.dto.FineResponseDto;
import com.example.library.fine.dto.FineSummaryDto;
import com.example.library.fine.entity.Fine;
import com.example.library.fine.entity.FineStatus;
import com.example.library.fine.mapper.FineMapper;
import com.example.library.fine.repository.FineRepository;
import com.example.library.loan.entity.Loan;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;
    private final FineMapper fineMapper;
    private final BigDecimal finePerDayAmount;

    public FineServiceImpl(FineRepository fineRepository, FineMapper fineMapper,@Value("10000") BigDecimal finePerDayAmount) {
        this.fineRepository = fineRepository;
        this.fineMapper = fineMapper;
        this.finePerDayAmount = finePerDayAmount;
    }

    @Override
    public FineResponseDto issueFine(Loan loan, int daysLate) {
        BigDecimal amount=finePerDayAmount.multiply(BigDecimal.valueOf(daysLate));

        Fine fine = Fine.builder()
                .loan(loan)
                .member(loan.getMember())
                .daysLate(daysLate)
                .amount(amount)
                .status(FineStatus.UNPAID)
                .issuedDate(LocalDate.now())
                .build();

        Fine savedFine = fineRepository.save(fine);
        return fineMapper.toResponseDto(savedFine);
    }

    @Override
    public FineResponseDto payFine(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine with ID " + fineId + " was not found"));

        if(fine.getStatus().equals(FineStatus.PAID)) {
            throw new BusinessRuleViolationException("This fine has already been paid");
        }

        fine.setStatus(FineStatus.PAID);
        fine.setPaidDate(LocalDate.now());
        Fine updateFine = fineRepository.save(fine);
        return fineMapper.toResponseDto(updateFine);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<FineResponseDto> getFinesByMember(Long memberId) {
        return fineRepository.findByMemberId(memberId)
                .stream()
                .map(fineMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FineSummaryDto getUnpaidSummaryByMember(Long memberId) {
        BigDecimal totalUnpaid = fineRepository.sumUnpaidAmountByMemberId(memberId);
        long unpaidCount = fineRepository.countByMemberIdAndStatus(memberId, FineStatus.UNPAID);

        return FineSummaryDto.builder()
                .memberId(memberId)
                .totalUnpaidAmount(totalUnpaid)
                .unpaidFineCount(unpaidCount)
                .build();
    }
}
