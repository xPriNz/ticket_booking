package pl.kocmon.booking;

import org.springframework.boot.SpringApplication;
import pl.kocmon.booking.ReservationService.services.TestClockConfiguration;

public class TestBookingApplication {

	public static void main(String[] args) {
		SpringApplication.from(BookingApplication::main)
                .with(TestcontainersConfiguration.class)
                .with(TestClockConfiguration.class).run(args);
	}

}
