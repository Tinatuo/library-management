package com.example.library.fine.controller;

import com.example.library.fine.dto.FineResponseDto;
import com.example.library.fine.dto.FineSummaryDto;
import com.example.library.fine.service.FineService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<FineResponseDto>> getFinesByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(fineService.getFinesByMember(memberId));
    }

    @GetMapping("/member/{memberId}/unpaid-summary")
    public ResponseEntity<FineSummaryDto> getUnpaidSummary(@PathVariable Long memberId) {
        return ResponseEntity.ok(fineService.getUnpaidSummaryByMember(memberId));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<FineResponseDto> payFine(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.payFine(id));
    }
}
