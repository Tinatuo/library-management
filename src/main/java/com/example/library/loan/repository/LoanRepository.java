package com.example.library.loan.repository;

import com.example.library.loan.entity.Loan;
import com.example.library.loan.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMemberId(Long memberId);

    Optional<Loan> findByBookIdAndStatus(Long bookId, LoanStatus status);

    boolean existsByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, LoanStatus status);
}
