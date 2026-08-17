package pl.kocmon.booking.ReservationService.models;

import jakarta.persistence.*;
import lombok.NonNull;

@Entity
public class OccurrenceSeat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occurrence_id", nullable = false)
    EventOccurrence occurrence;
    //Seat seat,

    @NonNull int price_amount;

    @Column(length = 3)
    @NonNull
    String currency;

    @NonNull
    OccurrenceSeat.AvailabilityStatus availability_status;

    public enum AvailabilityStatus {
        AVAILABLE,
        UNAVAILABLE
    }
}
