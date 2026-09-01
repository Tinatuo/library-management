package com.example.library.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component("currentUserService")
public class CurrentUserService {

    public boolean isSelf(Long memberId) {
        Long currentMemberId = getCurrentMemberId();
        return currentMemberId != null && currentMemberId.equals(memberId);
    }

    public Long getCurrentMemberId() {
        UserPrincipal principal = getPrincipalOrNull();
        return principal != null ? principal.getMemberId() : null;
    }

    public Long getCurrentUserId() {
        UserPrincipal principal = getPrincipalOrNull();
        return principal != null ? principal.getUserId() : null;
    }

    private UserPrincipal getPrincipalOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal;
    }
}
