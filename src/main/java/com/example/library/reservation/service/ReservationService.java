package com.example.library.reservation.service;

import com.example.library.book.entity.Book;
import com.example.library.reservation.dto.ReservationResponseDto;
import com.example.library.reservation.entity.Reservation;
import com.example.library.reservation.entity.ReservationStatus;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import com.example.library.common.dto.PageResponseDto;

import java.util.List;
import java.util.Optional;

public interface ReservationService {

    ReservationResponseDto reserveBook(Long bookId, Long memberId);

    ReservationResponseDto cancelReservation(Long reservationId);

    PageResponseDto<ReservationResponseDto> getReservationsByMember(Long memberId, int page, int size);

    PageResponseDto<ReservationResponseDto> getQueueForBook(Long bookId, int page, int size);

    boolean hasActiveReservation(Long bookId);

    Optional<Reservation> promoteNextInQueue(Book book);

    int expireStaleReadyReservations();

    void fulfillReservation(Long bookId, Long memberId);

    boolean hasReadyReservation(Long bookId, Long memberId);

    Long getOwnerMemberId(Long reservationId);

}
