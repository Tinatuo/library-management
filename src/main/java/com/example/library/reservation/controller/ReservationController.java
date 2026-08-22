package com.example.library.reservation.controller;

import com.example.library.reservation.dto.ReservationRequestDto;
import com.example.library.reservation.dto.ReservationResponseDto;
import com.example.library.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> reserveBook(@Valid @RequestBody ReservationRequestDto requestDto) {
        ReservationResponseDto reservation = reservationService.reserveBook(requestDto.getBookId(), requestDto.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDto> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<ReservationResponseDto>> getReservationsByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(reservationService.getReservationsByMember(memberId));
    }

    @GetMapping("/book/{bookId}/queue")
    public ResponseEntity<List<ReservationResponseDto>> getQueueForBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(reservationService.getQueueForBook(bookId));
    }
}
