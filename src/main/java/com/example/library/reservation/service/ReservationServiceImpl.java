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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.library.reservation.entity.ReservationStatus.READY;
import static com.example.library.reservation.entity.ReservationStatus.WAITING;


@Transactional
@Service
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
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with ID " + reservationId + " was not found"));

        if (reservation.getStatus() == ReservationStatus.FULFILLED) {
            throw new BusinessRuleViolationException("This reservation has already been fulfilled and cannot be cancelled");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessRuleViolationException("This reservation has already been cancelled");
        }

        boolean wasReady = reservation.getStatus() == ReservationStatus.READY;
        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation cancelledReservation = reservationRepository.save(reservation);

        if (wasReady) {
            Book book = cancelledReservation.getBook();
            Optional<Reservation> next = promoteNextInQueue(book);
            if (next.isEmpty()) {
                bookService.markAsAvailable(book.getId());
            }
        }

        return reservationMapper.toResponseDto(cancelledReservation, null);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByMember(Long memberId) {
        if (memberService.getMemberById(memberId)==null) {
            throw new ResourceNotFoundException("Member with ID " + memberId + " was not found");
        }
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(r -> reservationMapper.toResponseDto(r, resolveQueuePosition(r)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getQueueForBook(Long bookId) {
        List<Reservation> waiting = reservationRepository.findByBookIdAndStatusOrderByReservationDateAsc(bookId, ReservationStatus.WAITING);
        List<Reservation> ready = reservationRepository.findByBookIdAndStatusOrderByReservationDateAsc(bookId, ReservationStatus.READY);

        List<ReservationResponseDto> queue = new ArrayList<>();
        for (Reservation r : ready) {
            queue.add(reservationMapper.toResponseDto(r, 0));
        }
        for (int i = 0; i < waiting.size(); i++) {
            queue.add(reservationMapper.toResponseDto(waiting.get(i), i + 1));
        }
        return queue;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveReservation(Long bookId) {
        return reservationRepository.existsByBookIdAndStatusIn(bookId, List.of(WAITING,READY));
    }

    @Override
    public Optional<Reservation> promoteNextInQueue(Book book) {
        Optional<Reservation> nextReservation = reservationRepository
                .findFirstByBookIdAndStatusOrderByReservationDateAsc(book.getId(), ReservationStatus.WAITING);

        nextReservation.ifPresent(reservation -> {
            reservation.setStatus(ReservationStatus.READY);
            reservationRepository.save(reservation);
        });

        return nextReservation;
    }

    @Override
    public void fulfillReservation(Long bookId, Long memberId) {
        Reservation reservation = reservationRepository
                .findByBookIdAndMemberIdAndStatus(bookId, memberId, ReservationStatus.READY)
                .orElseThrow(() -> new ResourceNotFoundException("No ready reservation was found for this member and book"));

        reservation.setStatus(ReservationStatus.FULFILLED);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReadyReservation(Long bookId, Long memberId) {
        return reservationRepository.findByBookIdAndMemberIdAndStatus(bookId, memberId, ReservationStatus.READY).isPresent();
    }

    private Integer resolveQueuePosition(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.READY) {
            return 0;
        }
        if (reservation.getStatus() != ReservationStatus.WAITING) {
            return null;
        }
        List<Reservation> waiting = reservationRepository
                .findByBookIdAndStatusOrderByReservationDateAsc(reservation.getBook().getId(), ReservationStatus.WAITING);
        return waiting.indexOf(reservation) + 1;
    }
}
