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

    //@ManyToOne(fetch = FetchType.LAZY, optional = true)
    //@Nullable
    //long user_id,

    @Nullable String guest_email;
    @Nullable String guest_name;

    @NonNull
    ReservationStatus status;

    @Nullable Instant confirmed_at;
    @Nullable Instant cancelled_at;
    @Nullable Instant refunded_at;
}