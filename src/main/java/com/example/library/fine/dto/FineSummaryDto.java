package com.example.library.fine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineSummaryDto {

    private Long memberId;
    private BigDecimal totalUnpaidAmount;
    private long unpaidFineCount;
}
