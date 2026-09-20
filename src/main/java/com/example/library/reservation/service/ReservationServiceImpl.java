package com.example.library.reservation.service;

import com.example.library.book.entity.Book;
import com.example.library.book.entity.BookStatus;
import com.example.library.book.service.BookService;
import com.example.library.common.aop.Audited;
import com.example.library.common.dto.PageResponseDto;
import com.example.library.common.dto.PageResponseMapper;
import com.example.library.common.exception.BusinessRuleViolationException;
import com.example.library.common.exception.ResourceNotFoundException;
import com.example.library.member.entity.Member;
import com.example.library.member.service.MemberService;
import com.example.library.reservation.dto.ReservationResponseDto;
import com.example.library.reservation.entity.Reservation;
import com.example.library.reservation.entity.ReservationStatus;
import com.example.library.reservation.mapper.ReservationMapper;
import com.example.library.reservation.repository.ReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  BookService bookService,
                                  MemberService memberService,
                                  ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.bookService = bookService;
        this.memberService = memberService;
        this.reservationMapper = reservationMapper;
    }

    @Override
    @Audited(action = "RESERVATION_CREATE", details = "bookId=#{#bookId}, memberId=#{#memberId}")
    public ReservationResponseDto reserveBook(Long bookId, Long memberId) {
        Book book = bookService.getBookEntityById(bookId);
        Member member = memberService.getMemberEntityById(memberId);

        if (member.isMembershipExpired()) {
            throw new BusinessRuleViolationException(
                    "This member's membership has expired, so they cannot reserve books");
        }

        if (book.getStatus() == BookStatus.AVAILABLE) {
            throw new BusinessRuleViolationException(
                    "This book is currently available; it can be borrowed directly instead of reserved");
        }

        if (reservationRepository.existsByBookIdAndMemberIdAndStatusIn(
                bookId, memberId, List.of(WAITING, READY))) {
            throw new BusinessRuleViolationException(
                    "This member already has an active reservation for this book");
        }

        Reservation reservation = Reservation.builder()
                .book(book)
                .member(member)
                .reservationDate(LocalDateTime.now())
                .status(WAITING)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        long position = reservationRepository.countWaitingBefore(
                bookId, WAITING, savedReservation.getReservationDate(), savedReservation.getId()) + 1;

        return reservationMapper.toResponseDto(savedReservation, (int) position);
    }

    @Override
    @Audited(action = "RESERVATION_CANCEL", details = "reservationId=#{#reservationId}")
    public ReservationResponseDto cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation with ID " + reservationId + " was not found"));

        if (reservation.getStatus() == ReservationStatus.FULFILLED) {
            throw new BusinessRuleViolationException(
                    "This reservation has already been fulfilled and cannot be cancelled");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessRuleViolationException(
                    "This reservation has already been cancelled");
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
    public PageResponseDto<ReservationResponseDto> getReservationsByMember(
            Long memberId, int page, int size) {

        if (!memberService.existsById(memberId)) {
            throw new ResourceNotFoundException("Member with ID " + memberId + " was not found");
        }

        Page<ReservationResponseDto> result = reservationRepository.findByMemberId(
                memberId,
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Order.desc("reservationDate"),
                                Sort.Order.desc("id")
                        )
                )
        ).map(this::toResponseDtoWithQueuePosition);
        return PageResponseMapper.toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ReservationResponseDto> getQueueForBook(
            Long bookId, int page, int size) {

        bookService.getBookEntityById(bookId);

        Page<ReservationResponseDto> result = reservationRepository.findActiveQueueByBookId(
                        bookId,
                        List.of(WAITING, READY),
                        PageRequest.of(page, size))
                .map(this::toResponseDtoWithQueuePosition);

        return PageResponseMapper.toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveReservation(Long bookId) {
        return reservationRepository.existsByBookIdAndStatusIn(bookId, List.of(WAITING, READY));
    }

    @Override
    public Optional<Reservation> promoteNextInQueue(Book book) {
        Optional<Reservation> nextReservation = reservationRepository
                .findFirstByBookIdAndStatusOrderByReservationDateAscIdAsc(
                        book.getId(), WAITING);

        nextReservation.ifPresent(reservation -> {
            reservation.setStatus(READY);
            reservationRepository.save(reservation);
        });

        return nextReservation;
    }

    @Override
    @Audited(action="RESERVATION_FULFILL", details="bookId=#{#bookId}, memberId=#{#memberId}")
    public void fulfillReservation(Long bookId, Long memberId) {
        Reservation reservation = reservationRepository
                .findByBookIdAndMemberIdAndStatus(bookId, memberId, READY)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No ready reservation was found for this member and book"));

        reservation.setStatus(ReservationStatus.FULFILLED);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReadyReservation(Long bookId, Long memberId) {
        return reservationRepository
                .findByBookIdAndMemberIdAndStatus(bookId, memberId, READY)
                .isPresent();
    }

    private ReservationResponseDto toResponseDtoWithQueuePosition(Reservation reservation) {
        Integer queuePosition = null;

        if (reservation.getStatus() == READY) {
            queuePosition = 0;
        } else if (reservation.getStatus() == WAITING) {
            queuePosition = (int) reservationRepository.countWaitingBefore(
                    reservation.getBook().getId(),
                    WAITING,
                    reservation.getReservationDate(),
                    reservation.getId()) + 1;
        }

        return reservationMapper.toResponseDto(reservation, queuePosition);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getOwnerMemberId(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation with ID " + reservationId + " was not found"));
        return reservation.getMember().getId();
    }
}
