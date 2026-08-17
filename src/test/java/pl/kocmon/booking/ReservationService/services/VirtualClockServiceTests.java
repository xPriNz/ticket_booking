package pl.kocmon.booking.ReservationService.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@SpringBootTest
public class VirtualClockServiceTests {
    VirtualClockService clock = new VirtualClockService();

    @Test
    @Timeout(5)
    void shouldSleepInCorrectOrder() throws InterruptedException {
        var second = Duration.ofSeconds(1);

        AtomicInteger tested = new AtomicInteger(-1);
        var delays = new ArrayList<>(IntStream.range(0, 100).boxed().toList());
        Collections.shuffle(delays);

        var threads = new HashMap<Integer, Thread>();
        var now = clock.now();

        for(int delay: delays) {
            var thread = Thread.ofVirtual().start(() -> {
                clock.sleepUntil(now.plus(second.multipliedBy(delay + 1)));
                tested.set(delay);
            });
            threads.put(delay, thread);
        }

        assert tested.get() == -1;
        clock.advance(second);
        for(int i = 0; i < 100; i++) {
            clock.advance(second);
            threads.get(i).join();
            assert tested.get() == i : String.format("Expected %d, got %d", i, tested.get());
        }
    }
}
