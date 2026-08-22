package com.example.library.fine.dto;

import com.example.library.fine.entity.FineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineResponseDto {

    private Long id;
    private Long loanId;
    private Long bookId;
    private String bookTitle;
    private Long memberId;
    private String memberFullName;
    private Integer daysLate;
    private BigDecimal amount;
    private FineStatus status;
    private LocalDate issuedDate;
    private LocalDate paidDate;
}
