package pl.kocmon.booking.ReservationService.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

@MappedSuperclass
@Getter
public abstract class BaseEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @NonNull
    protected long id;

    @NonNull @Column(nullable = false, updatable = false) protected Instant created_at;
    @NonNull protected Instant updated_at;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        created_at = now;
        updated_at = now;
    }

    @PreUpdate
    void preUpdate() {
        updated_at = Instant.now();
    }
}
