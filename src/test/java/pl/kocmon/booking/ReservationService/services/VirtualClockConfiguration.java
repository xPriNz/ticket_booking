package pl.kocmon.booking.ReservationService.services;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class VirtualClockConfiguration {

    @Bean
    @Primary
    ClockService clockServiceTest() {
        return new VirtualClockService();
    }
}

