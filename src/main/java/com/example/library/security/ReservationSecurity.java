package com.example.library.security;

import com.example.library.auth.security.CurrentUserService;
import com.example.library.reservation.service.ReservationService;
import org.springframework.stereotype.Component;


@Component("reservationSecurity")
public class ReservationSecurity {

    private final ReservationService reservationService;
    private final CurrentUserService currentUserService;

    public ReservationSecurity(ReservationService reservationService, CurrentUserService currentUserService) {
        this.reservationService = reservationService;
        this.currentUserService = currentUserService;
    }

    public boolean isOwner(Long reservationId) {
        Long ownerMemberId = reservationService.getOwnerMemberId(reservationId);
        return currentUserService.isSelf(ownerMemberId);
    }
}
