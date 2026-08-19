package pl.kocmon.booking.ReservationService.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import pl.kocmon.booking.ReservationService.models.Expirable;
import pl.kocmon.booking.ReservationService.repositories.ExpirableRepository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

public class ExpirationService<T extends Expirable, REPO extends ExpirableRepository<T>> {
    @Autowired ClockService clockService;
    @Autowired REPO repository;
/*
    private record TrackedExpirableData(
        Instant expires_at,
        long id
    )
        implements Comparable<TrackedExpirableData>
    {
        @Override
        public int compareTo(TrackedExpirableData other) {
            var exp_at = this.expires_at.compareTo(other.expires_at);
            return exp_at == 0 ? Long.compare(this.id, other.id) : exp_at;
        }
    }

    private record TrackedExpirable(
        TrackedExpirableData expirable,
        Consumer<Long> updateCallback,
        boolean valid
    ) {}

    private static class ExpirableStore {
        Map<Long, TrackedExpirable> objectToTracked = new HashMap<>();
        SortedSet<TrackedExpirableData> unexpiredObjectIds = new TreeSet<>();

        void put(TrackedExpirable tracked) {
            objectToTracked.put(tracked.expirable.id(), tracked);
            unexpiredObjectIds.add(tracked.expirable);
        }

        TrackedExpirable first() {
            return objectToTracked.get(unexpiredObjectIds.first().id);
        }

        TrackedExpirable remove(long id) {
            TrackedExpirable tracked = objectToTracked.remove(id);
            if(tracked != null) {
                unexpiredObjectIds.remove(tracked.expirable);
            }
            return tracked;
        }
    }

    private record NextProcess(Thread thread, Instant wakeUpInstant, TrackedExpirable nextExpirable) {}

    ExpirableStore store;

    //Instant wakeUpInstant;
    //TrackedExpirable nextExpirable;
    //Optional<Thread> nextProcess;
    Optional<NextProcess> nextProcess;
    Lock objectLock;

    private void sleepUntil(Instant end) {

    }

    private void waitAndExpire(TrackedExpirable tracked) {
        sleepUntil(tracked.expirable.expires_at());
        objectLock.lock();
        try {
            tracked.updateCallback.accept(tracked.expirable.id);
            store.remove(tracked.expirable.id);
        }
        finally {
            nextProcess = Optional.empty();
            objectLock.unlock();
            updateTrackedStatus();
        }
    }

    void updateTrackedStatus() {
        var next = store.first();
        if(next == null) {
            nextProcess = Optional.empty();
        }

        var wakeup = nextProcess.isPresent() ? nextProcess.get().wakeUpInstant : Instant.MIN;

        if(next.expirable.expires_at.isBefore(wakeup)) {
            if(nextProcess.isPresent()) {
                nextProcess.get().thread.interrupt();
            }
            nextProcess = Optional.of(new NextProcess(
                    Thread.ofVirtual().start(() -> waitAndExpire(next)),
                    next.expirable().expires_at,
                    next)
            );
        }
    }

    public void insert(Expirable expirable, Consumer<Long> updateCallback) {
        var tracked = new TrackedExpirable(
                new TrackedExpirableData(expirable.getExpires_at(), expirable.getId()),
                updateCallback,
                true
        );
        store.put(tracked);
        updateTrackedStatus();
    }

    public void invalidate(Expirable expirable) {
        store.remove(expirable.getId());
        //if(nextExpirable.expirable.id == expirable.getId()) {

        //}
    }
 */
    public ExpirationService() {
        /*
        for(var pendingExpirable: repository.findByExpiresAtNotNull()) {
            Thread.ofVirtual().start(() -> waitAndExpire(
                    pendingExpirable.getExpirationTime(),
                    pendingExpirable.getId(),
                    pendingExpirable.getExpireIdempotencyKey())
            );
        }*/
    }

    public long save(T expirable, Instant instant) {
        var idempotencyKey = expirable.setExpirationTime(instant);
        var newExp = repository.save(expirable);

        var id = newExp.getId();

        Thread.ofVirtual().start(
            () -> waitAndExpire(instant, id, idempotencyKey)
        );
        return id;
    }

    private void waitAndExpire(Instant expirationTime, long id, long idempotencyKey) {
        // TODO refactor
        System.out.println("a");

        clockService.sleepUntil(expirationTime);
        System.out.println("b");
        var exp = repository.findById(id).orElseThrow( // TODO do we even care? deleted expirables could be safely discarded
                () -> new IndexOutOfBoundsException("Expirable id " + id + " not found")
        );
        System.out.println("c");
        if(exp.tryExpire(clockService.now(), idempotencyKey)) {
            System.out.println("seved");
            repository.save(exp);
        }
        System.out.println("d");
    }

    // TODO tests - a,b
    // 1. insert a,a
    // 2. insert a,b
    // 3. insert
}
