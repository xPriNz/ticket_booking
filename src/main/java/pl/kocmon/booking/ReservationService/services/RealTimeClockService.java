package pl.kocmon.booking.ReservationService.services;

import java.time.Instant;

public class RealTimeClockService implements ClockService {
    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public void sleepUntil(Instant instant) {
        try {
            Thread.sleep(now().until(instant));
        }
        catch (InterruptedException _) {
            // TODO how to handle?
        }
    }
}
