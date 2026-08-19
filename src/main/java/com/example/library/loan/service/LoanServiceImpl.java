package com.example.library.loan.service;

import com.example.library.book.entity.Book;
import com.example.library.book.entity.BookStatus;
import com.example.library.book.service.BookService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    private static final int LOAN_PERIOD_DAYS = 14;

    private final LoanRepository loanRepository;
    private final MemberService memberService;
    private final BookService bookService;
    private final LoanMapper loanMapper;

    public LoanServiceImpl(LoanRepository loanRepository,
                           MemberService memberService,
                           BookService bookService,
                           LoanMapper loanMapper) {
        this.loanRepository = loanRepository;
        this.memberService = memberService;
        this.bookService = bookService;
        this.loanMapper = loanMapper;
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
    @Transactional(readOnly = true)
    public List<LoanResponseDto> getAllLoans() {
        return loanRepository.findAll()
                .stream()
                .map(loanMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponseDto> getLoansByMember(Long memberId) {
        if (!memberService.existsById(memberId)) {
            throw new ResourceNotFoundException("Member with ID " + memberId + " was not found");
        }
        return loanRepository.findByMemberId(memberId)
                .stream()
                .map(loanMapper::toResponseDto)
                .toList();
    }
}

