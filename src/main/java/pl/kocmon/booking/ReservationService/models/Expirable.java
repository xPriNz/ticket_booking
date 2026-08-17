package pl.kocmon.booking.ReservationService.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.Instant;

@MappedSuperclass
@Getter
public class Expirable extends BaseEntity {
    @Nullable Instant expires_at;

    boolean isExpired(Instant now) {
        return expires_at != null && expires_at.isAfter(now);
    }

    // TODO prePersist, expiration service etc
}
