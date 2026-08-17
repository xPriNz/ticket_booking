package pl.kocmon.booking.ReservationService.services;

import java.time.Duration;
import java.time.Instant;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public class VirtualClockService implements ClockService {

    Instant now = Instant.MIN;
    TreeMap<Instant, CountDownLatch> semaphores = new TreeMap<>();

    @Override
    public synchronized Instant now() {
        return now;
    }

    @Override
    public void sleepUntil(Instant instant) {
        try {
            CountDownLatch countdown;
            synchronized(this) {
                if (instant.isBefore(now)) {
                    //System.out.println("skipping " + instant.toString());
                    return;
                }

                semaphores.putIfAbsent(instant, new CountDownLatch(1));
                countdown = semaphores.get(instant);
            }
            //System.out.println("awaiting for " + instant.toString());
            countdown.await();
            //System.out.println("done " + instant.toString());
        }
        catch (InterruptedException e) {
            throw new RuntimeException(String.format("Got InterruptedException: %s", e.getMessage()));
        }
    }

    public void advance(Duration duration) {
        synchronized(this) {
            now = now.plus(duration);
            //System.out.println("advancing to " + now.toString());

            var entry = semaphores.firstEntry();
            while (entry != null && entry.getKey().isBefore(now)) {
                entry.getValue().countDown();
                //System.out.println("removing " + entry.getKey().toString());
                semaphores.remove(entry.getKey());
                entry = semaphores.firstEntry();
            }
        }
    }
}
