package pl.kocmon.booking.ReservationService.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

@Entity
public class Reservation extends Expirable {
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occurrence_id")
    EventOccurrence occurrence;

    @Nullable String guest_email;
    @Nullable String guest_name;

    @Nullable Instant confirmed_at;
    @Nullable Instant cancelled_at;
    @Nullable Instant refunded_at;

    public enum Status {
        PENDING_PAYMENT,
        CONFIRMED,
        EXPIRED,
        PAYMENT_FAILED,
        CANCELLED,
        REFUNDED
    }

    @NonNull Status status;

    @Override
    protected boolean onExpire() {
        return switch(status) {
            case CONFIRMED, CANCELLED, REFUNDED -> false;
            case PENDING_PAYMENT, PAYMENT_FAILED, EXPIRED -> {
                status = Status.EXPIRED;
                yield true;
            }
        };
    }
}