package pl.kocmon.booking.ReservationService.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

@MappedSuperclass
public abstract class Expirable extends BaseEntity {
    @Nullable protected Instant expiresAt;
    @Getter protected long expireIdempotencyKey;

    public final boolean isExpired(Instant now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }

    /// Set a new expiration time and return the new idempotency key
    public final long setExpirationTime(Instant now) {
        expiresAt = now;
        expireIdempotencyKey += 1;
        return expireIdempotencyKey;
    }

    public Instant getExpirationTime() {
        return expiresAt;
    }

    public final boolean tryExpire(Instant now, long idempotencyKey) {
        System.out.printf("trying %d now:%s exp:%s    ik:%d vs my:%d%n", id, now, expiresAt, idempotencyKey, expireIdempotencyKey);
        if(isExpired(now) && idempotencyKey == expireIdempotencyKey) {
            return onExpire();
        }
        return false;
    }

    abstract protected boolean onExpire();

    // TODO prePersist, expiration service etc
}
