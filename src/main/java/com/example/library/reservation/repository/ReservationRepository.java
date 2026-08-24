package com.example.library.reservation.repository;

import com.example.library.reservation.entity.Reservation;
import com.example.library.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByMemberId(Long memberId, Pageable pageable);

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.book.id = :bookId
              AND r.status IN :statuses
            ORDER BY CASE WHEN r.status = com.example.library.reservation.entity.ReservationStatus.READY
                          THEN 0 ELSE 1 END,
                     r.reservationDate ASC,
                     r.id ASC
            """)
    Page<Reservation> findActiveQueueByBookId(
            @Param("bookId") Long bookId,
            @Param("statuses") List<ReservationStatus> statuses,
            Pageable pageable);

    Optional<Reservation> findFirstByBookIdAndStatusOrderByReservationDateAscIdAsc(
            Long bookId, ReservationStatus status);

    Optional<Reservation> findByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, ReservationStatus status);

    boolean existsByBookIdAndMemberIdAndStatusIn(Long bookId, Long memberId, List<ReservationStatus> statuses);

    boolean existsByBookIdAndStatusIn(Long bookId, List<ReservationStatus> statuses);

    @Query("""
            SELECT COUNT(r) FROM Reservation r
            WHERE r.book.id = :bookId
              AND r.status = :status
              AND (r.reservationDate < :reservationDate
                   OR (r.reservationDate = :reservationDate AND r.id < :reservationId))
            """)
    long countWaitingBefore(
            @Param("bookId") Long bookId,
            @Param("status") ReservationStatus status,
            @Param("reservationDate") LocalDateTime reservationDate,
            @Param("reservationId") Long reservationId);
}
