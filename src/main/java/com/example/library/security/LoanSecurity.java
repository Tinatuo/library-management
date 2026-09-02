package com.example.library.security;

import com.example.library.auth.security.CurrentUserService;
import com.example.library.loan.service.LoanService;
import org.springframework.stereotype.Component;


@Component("loanSecurity")
public class LoanSecurity {

    private final LoanService loanService;
    private final CurrentUserService currentUserService;

    public LoanSecurity(LoanService loanService, CurrentUserService currentUserService) {
        this.loanService = loanService;
        this.currentUserService = currentUserService;
    }

    public boolean isOwner(Long loanId) {
        Long ownerMemberId = loanService.getLoanById(loanId).getMemberId();
        return currentUserService.isSelf(ownerMemberId);
    }
}
