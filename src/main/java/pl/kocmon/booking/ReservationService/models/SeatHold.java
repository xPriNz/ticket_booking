package pl.kocmon.booking.ReservationService.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

@Entity
@Getter
public class SeatHold extends Expirable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occurrence_id", nullable = false)
    OccurrenceSeat occurrence_seat;
    //User user,

    String guest_email;
    String guest_name;

    Status status;

    @Nullable Instant converted_at;
    @Nullable Instant cancelled_at;

    public enum Status {
        ACTIVE,
        CONVERTED,
        EXPIRED,
        CANCELLED
    }

    @Override
    protected boolean onExpire() {
        return switch(status) {
            case CANCELLED, CONVERTED -> false;
            case ACTIVE, EXPIRED -> {
                status = Status.EXPIRED;
                yield true;
            }
        };
    }
}
