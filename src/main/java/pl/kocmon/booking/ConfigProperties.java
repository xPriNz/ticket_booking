package pl.kocmon.booking;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.kocmon.booking.ReservationService.models.Expirable;
import pl.kocmon.booking.ReservationService.services.ClockService;
import pl.kocmon.booking.ReservationService.services.RealTimeClockService;

@Configuration
public class ConfigProperties {
    @Bean
    ClockService clockService() {
        return new RealTimeClockService();
    }
}
