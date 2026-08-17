package pl.kocmon.booking.ReservationService.services;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public interface ClockService {
    Instant now();
    void sleepUntil(Instant instant);
    default void sleepFor(Duration duration) {
        var instant = now().plus(duration);
        sleepUntil(instant);
    }
}
