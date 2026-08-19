package com.example.library.reservation.entity;

public enum ReservationStatus {
    /** Waiting in the queue; the book is still with someone else. */
    WAITING,
    /** At the front of the queue; the book is being held for this member to pick up. */
    READY,
    /** The member borrowed the reserved book. */
    FULFILLED,
    /** The member cancelled the reservation before it was fulfilled. */
    CANCELLED
}
