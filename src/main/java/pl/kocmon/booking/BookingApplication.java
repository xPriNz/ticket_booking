package pl.kocmon.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingApplication {

	public static void main(String[] args) {
		SpringApplication.from(BookingApplication::main)
                .with(ConfigProperties.class)
                .run(args);
	}

}
