package com.example.library.reservation.service;

import com.example.library.book.entity.Book;
import com.example.library.reservation.dto.ReservationResponseDto;
import com.example.library.reservation.entity.Reservation;
import com.example.library.reservation.entity.ReservationStatus;

import java.util.List;
import java.util.Optional;

public interface ReservationService {

    ReservationResponseDto reserveBook(Long bookId, Long memberId);

    ReservationResponseDto cancelReservation(Long reservationId);

    List<ReservationResponseDto> getReservationsByMember(Long memberId);

    List<ReservationResponseDto> getQueueForBook(Long bookId);

    boolean hasActiveReservation(Long bookId);

    Optional<Reservation> promoteNextInQueue(Book book);

    void fulfillReservation(Long bookId, Long memberId);

    boolean hasReadyReservation(Long bookId, Long memberId);
}
