package pl.kocmon.booking.ReservationService.services;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class TestClockConfiguration {

    @Bean
    ClockService clockService() {
        return new VirtualClockService();
    }
}

