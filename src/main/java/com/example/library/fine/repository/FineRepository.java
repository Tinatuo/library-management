package com.example.library.fine.repository;

import com.example.library.fine.entity.Fine;
import com.example.library.fine.entity.FineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {

    Page<Fine> findByMemberId(Long memberId,Pageable pageable);

    Page<Fine> findByMemberIdAndStatus(Long memberId, FineStatus status, Pageable pageable);

    long countByMemberIdAndStatus(Long memberId, FineStatus status);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.member.id = :memberId AND f.status = 'UNPAID'")
    BigDecimal sumUnpaidAmountByMemberId(@Param("memberId") Long memberId);
}
