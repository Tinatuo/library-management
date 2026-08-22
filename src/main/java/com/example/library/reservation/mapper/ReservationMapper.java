package com.example.library.reservation.mapper;

import com.example.library.reservation.dto.ReservationRequestDto;
import com.example.library.reservation.dto.ReservationResponseDto;
import com.example.library.reservation.entity.Reservation;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReservationMapper {

    public ReservationResponseDto toResponseDto(Reservation reservation, Integer queuePosition) {
        return ReservationResponseDto.builder()
                .id(reservation.getId())
                .bookId(reservation.getBook().getId())
                .bookTitle(reservation.getBook().getTitle())
                .memberId(reservation.getMember().getId())
                .memberFullName(reservation.getMember().getFullName())
                .reservationDate(reservation.getReservationDate())
                .status(reservation.getStatus())
                .queuePosition(queuePosition)
                .build();
    }
}
