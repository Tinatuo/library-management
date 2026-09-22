package com.example.library.reservation.scheduler;

import com.example.library.reservation.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class ReservationHoldScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationHoldScheduler.class);

    private final ReservationService reservationService;

    public ReservationHoldScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "${app.reservation.expiry-cron:0 0 2 * * *}")
    public void expireStaleReservationHolds() {
        int expiredCount = reservationService.expireStaleReadyReservations();
        if (expiredCount > 0) {
            log.info("Expired {} stale reservation hold(s)", expiredCount);
        }
    }
}
