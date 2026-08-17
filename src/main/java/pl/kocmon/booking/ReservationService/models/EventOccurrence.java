package pl.kocmon.booking.ReservationService.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class EventOccurrence extends BaseEntity {
    // Event event;
    // Room room;

    Instant starts_at;
    Instant ends_at;
    Instant sales_starts_at;
    Instant sales_end_at;

    OccurrenceStatus status;
}
