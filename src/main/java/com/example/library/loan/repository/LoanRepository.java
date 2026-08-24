package com.example.library.loan.repository;

import com.example.library.loan.entity.Loan;
import com.example.library.loan.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @EntityGraph(attributePaths = {"book", "member"})
    Page<Loan> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"book", "member"})
    Page<Loan> findByMemberId(Long memberId, Pageable pageable);

    boolean existsByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, LoanStatus status);
}
