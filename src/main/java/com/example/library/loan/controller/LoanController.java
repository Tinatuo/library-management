package com.example.library.loan.controller;

import com.example.library.common.dto.PageResponseDto;
import com.example.library.loan.dto.LoanRequestDto;
import com.example.library.loan.dto.LoanResponseDto;
import com.example.library.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<LoanResponseDto> borrowBook(@Valid @RequestBody LoanRequestDto requestDto) {
        LoanResponseDto loan = loanService.borrowBook(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<LoanResponseDto> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnBook(id));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<LoanResponseDto> renewLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.renewLoan(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDto> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<LoanResponseDto>> getAllLoans(int page, int size) {
        return ResponseEntity.ok(loanService.getAllLoans(page, size));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<PageResponseDto<LoanResponseDto>> getLoansByMember(@PathVariable Long memberId, @RequestParam(defaultValue ="0") int page, @RequestParam(defaultValue ="5") int size) {
        return ResponseEntity.ok(loanService.getLoansByMember(memberId,page,size));
    }
}
