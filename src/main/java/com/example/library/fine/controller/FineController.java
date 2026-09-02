package com.example.library.fine.controller;

import com.example.library.common.dto.PageResponseDto;
import com.example.library.fine.dto.FineResponseDto;
import com.example.library.fine.dto.FineSummaryDto;
import com.example.library.fine.service.FineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN',@currentUserService.isSelf(#memberId))")
    public ResponseEntity<PageResponseDto<FineResponseDto>> getFinesByMember(@PathVariable Long memberId, @RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(fineService.getFinesByMember(memberId, page, size));
    }

    @GetMapping("/member/{memberId}/unpaid-summary")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN',@currentUserService.isSelf(#memberId))")
    public ResponseEntity<FineSummaryDto> getUnpaidSummary(@PathVariable Long memberId) {
        return ResponseEntity.ok(fineService.getUnpaidSummaryByMember(memberId));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ResponseEntity<FineResponseDto> payFine(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.payFine(id));
    }
}
