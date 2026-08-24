package com.example.library.loan.service;

import com.example.library.book.entity.Book;
import com.example.library.book.entity.BookStatus;
import com.example.library.book.service.BookService;
import com.example.library.common.dto.PageResponseDto;
import com.example.library.common.dto.PageResponseMapper;
import com.example.library.common.exception.BusinessRuleViolationException;
import com.example.library.common.exception.ResourceNotFoundException;
import com.example.library.loan.dto.LoanRequestDto;
import com.example.library.loan.dto.LoanResponseDto;
import com.example.library.loan.entity.Loan;
import com.example.library.loan.entity.LoanStatus;
import com.example.library.loan.mapper.LoanMapper;
import com.example.library.loan.repository.LoanRepository;
import com.example.library.member.entity.Member;
import com.example.library.member.service.MemberService;
import com.example.library.reservation.service.ReservationService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@Getter
@Setter
public class LoanServiceImpl implements LoanService {

    private static final int LOAN_PERIOD_DAYS = 14;

    private final LoanRepository loanRepository;
    private final MemberService memberService;
    private final BookService bookService;
    private final LoanMapper loanMapper;
    private final int maxRenewalCount;
    private final int renewalExtensionDays;
    private final ReservationService reservationService;

    public LoanServiceImpl(LoanRepository loanRepository,
                           MemberService memberService,
                           BookService bookService,
                           LoanMapper loanMapper,
                           @Value("2") int maxRenewalCount,
                           @Value("14") int renewalExtensionDays,
                           ReservationService reservationService) {
        this.loanRepository = loanRepository;
        this.memberService = memberService;
        this.bookService = bookService;
        this.loanMapper = loanMapper;
        this.maxRenewalCount = maxRenewalCount;
        this.renewalExtensionDays = renewalExtensionDays;
        this.reservationService = reservationService;
    }

    @Override
    public LoanResponseDto borrowBook(LoanRequestDto requestDto) {
        Book book = bookService.getBookEntityById(requestDto.getBookId());

        Member member = memberService.getMemberEntityById(requestDto.getMemberId());

        if (member.isMembershipExpired()) {
            throw new BusinessRuleViolationException("This member's membership has expired, so they cannot borrow books");
        }

        if (book.getStatus() == BookStatus.BORROWED) {
            throw new BusinessRuleViolationException("This book is currently borrowed by another member");
        }

        if (loanRepository.existsByBookIdAndMemberIdAndStatus(book.getId(), member.getId(), LoanStatus.ACTIVE)) {
            throw new BusinessRuleViolationException("This member has already borrowed this book and has not returned it yet");
        }

        LocalDate loanDate = LocalDate.now();
        Loan loan = Loan.builder()
                .book(book)
                .member(member)
                .loanDate(loanDate)
                .dueDate(loanDate.plusDays(LOAN_PERIOD_DAYS))
                .status(LoanStatus.ACTIVE)
                .build();

        bookService.markAsBorrowed(book.getId());

        Loan savedLoan = loanRepository.save(loan);
        return loanMapper.toResponseDto(savedLoan);
    }

    @Override
    public LoanResponseDto returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan with ID " + loanId + " was not found"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleViolationException("This book has already been returned");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Book book = loan.getBook();
        bookService.markAsAvailable(book.getId());

        Loan updatedLoan = loanRepository.save(loan);
        return loanMapper.toResponseDto(updatedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponseDto getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan with ID " + id + " was not found"));
        return loanMapper.toResponseDto(loan);
    }


    @Override
    public LoanResponseDto renewLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan with ID " + loanId + " was not found"));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Only active (not yet returned) loans can be renewed");
        }

        if (loan.getMember().isMembershipExpired()) {
            throw new BusinessRuleViolationException("This member's membership has expired, so they cannot renew loans");
        }

        LocalDate today = LocalDate.now();
        if (today.isAfter(loan.getDueDate())) {
            throw new BusinessRuleViolationException("This loan is already overdue; renewal must be requested before the due date");
        }

        if (loan.getRenewalCount() >= maxRenewalCount) {
            throw new BusinessRuleViolationException("This loan has already reached the maximum number of renewals (" + maxRenewalCount + ")");
        }

        if (reservationService.hasActiveReservation(loan.getBook().getId())) {
            throw new BusinessRuleViolationException("This book has been reserved by another member and cannot be renewed");
        }

        loan.setDueDate(loan.getDueDate().plusDays(renewalExtensionDays));
        loan.setRenewalCount(loan.getRenewalCount() + 1);

        Loan renewedLoan = loanRepository.save(loan);
        return loanMapper.toResponseDto(renewedLoan);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<LoanResponseDto> getAllLoans(int page, int size) {
        Page<LoanResponseDto> result=loanRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
                .map(loanMapper::toResponseDto);
        return PageResponseMapper.toPageResponse(result);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<LoanResponseDto> getLoansByMember(Long memberId, int page, int size) {
        if (!memberService.existsById(memberId)) {
            throw new ResourceNotFoundException("Member with ID " + memberId + " was not found");
        }
        Page<LoanResponseDto> result = loanRepository.findByMemberId(
                        memberId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
                .map(loanMapper::toResponseDto);
        return PageResponseMapper.toPageResponse(result);
    }
}

