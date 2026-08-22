package com.example.library.reservation.service;

import com.example.library.book.entity.Book;
import com.example.library.book.entity.BookStatus;
import com.example.library.book.service.BookService;
import com.example.library.common.exception.BusinessRuleViolationException;
import com.example.library.common.exception.ResourceNotFoundException;
import com.example.library.member.entity.Member;
import com.example.library.member.service.MemberService;
import com.example.library.reservation.dto.ReservationResponseDto;
import com.example.library.reservation.entity.Reservation;
import com.example.library.reservation.entity.ReservationStatus;
import com.example.library.reservation.mapper.ReservationMapper;
import com.example.library.reservation.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.library.reservation.entity.ReservationStatus.READY;
import static com.example.library.reservation.entity.ReservationStatus.WAITING;

public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookService bookService;
    private final MemberService memberService;
    private final ReservationMapper reservationMapper;


    public ReservationServiceImpl(ReservationRepository reservationRepository, BookService bookService,
                                  MemberService memberService, ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.bookService = bookService;
        this.memberService = memberService;
        this.reservationMapper = reservationMapper;
    }


    @Override
    public ReservationResponseDto reserveBook(Long bookId, Long memberId) {
        Book book = bookService.getBookEntityById(bookId);

        Member member = memberService.getMemberEntityById(memberId);

        if (member.isMembershipExpired()) {
            throw new BusinessRuleViolationException("This member's membership has expired, so they cannot reserve books");
        }

        if (book.getStatus() == BookStatus.AVAILABLE) {
            throw new BusinessRuleViolationException("This book is currently available; it can be borrowed directly instead of reserved");
        }

        if (reservationRepository.existsByBookIdAndMemberIdAndStatusIn(bookId, memberId, List.of(WAITING,READY))) {
            throw new BusinessRuleViolationException("This member already has an active reservation for this book");
        }


        Reservation reservation = Reservation.builder()
                .book(book)
                .member(member)
                .reservationDate(LocalDateTime.now())
                .status(WAITING)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        int position = reservationRepository.findByBookIdAndStatusOrderByReservationDateAsc(bookId, WAITING).size();
        return reservationMapper.toResponseDto(savedReservation, position);
    }


    @Override
    public ReservationResponseDto cancelReservation(Long reservationId) {
        return null;
    }

    @Override
    public List<ReservationResponseDto> getReservationsByMember(Long memberId) {
        return List.of();
    }

    @Override
    public List<ReservationResponseDto> getQueueForBook(Long bookId) {
        return List.of();
    }

    @Override
    public boolean hasActiveReservation(Long bookId) {
        return false;
    }

    @Override
    public Optional<Reservation> promoteNextInQueue(Book book) {
        return Optional.empty();
    }

    @Override
    public void fulfillReservation(Long bookId, Long memberId) {

    }

    @Override
    public boolean hasReadyReservation(Long bookId, Long memberId) {
        return false;
    }
}
