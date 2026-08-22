package com.example.library.reservation.repository;

import com.example.library.reservation.entity.Reservation;
import com.example.library.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByBookIdAndStatusOrderByReservationDateAsc(Long bookId, ReservationStatus status);

    Optional<Reservation> findFirstByBookIdAndStatusOrderByReservationDateAsc(Long bookId, ReservationStatus status);

    Optional<Reservation> findByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, ReservationStatus status);

    boolean existsByBookIdAndMemberIdAndStatusIn(Long bookId, Long memberId, List<ReservationStatus> statuses);

    boolean existsByBookIdAndStatusIn(Long bookId, List<ReservationStatus> statuses);
}

