package pl.kocmon.booking.ReservationService.models;

public enum ReservationStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    EXPIRED,
    PAYMENT_FAILED,
    CANCELLED,
    REFUNDED
}
