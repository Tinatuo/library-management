package com.example.library.reservation.dto;

import com.example.library.reservation.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponseDto {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long memberId;
    private String memberFullName;
    private LocalDateTime reservationDate;
    private ReservationStatus status;
    private Integer queuePosition;
}
