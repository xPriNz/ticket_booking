package pl.kocmon.booking.ReservationService.services;

import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import pl.kocmon.booking.ReservationService.models.Expirable;
import pl.kocmon.booking.ReservationService.repositories.ExpirableRepository;

import java.time.Duration;

@Entity
@NoArgsConstructor
class TestExpirable extends Expirable {
    public boolean shouldExpire;
    public boolean expired = false;

    public TestExpirable(boolean shouldExpire) {
        this.shouldExpire = shouldExpire;
    }

    @Override
    protected boolean onExpire() {
        if(shouldExpire) {
            expired = true;
            return true;
        }
        return false;
    }
}

@org.springframework.stereotype.Repository
interface Repository extends ExpirableRepository<TestExpirable> {}

@Service
class ExpService extends ExpirationService<TestExpirable, Repository> {}


@SpringBootTest
@Import(VirtualClockConfiguration.class)
public class ExpirationServiceTests {
    @Autowired ClockService clockService;
    VirtualClockService clock;

    @Autowired ExpService exp;
    @Autowired Repository repo;

    @BeforeEach
    void setup() {
        clock = (VirtualClockService) clockService;
    }

    @Test
    void shouldExpireCorrectly() throws InterruptedException {
        var exp1id = exp.save(new TestExpirable(true),  clock.now().plusSeconds(10));
        var exp2id = exp.save(new TestExpirable(true),  clock.now().plusSeconds(20));

        System.out.println(repo.findAll().iterator().next().getId());

        clock.advance(Duration.ofSeconds(12));
        Thread.sleep(1000);
        assert repo.findById(exp1id).get().expired : "Expirable 1 must expire";
        assert !repo.findById(exp2id).get().expired : "Expirable 2 expired too soon";

        clock.advance(Duration.ofSeconds(10));
        Thread.sleep(1000);
        assert repo.findById(exp2id).get().expired : "Expirable 2 must expire";
    }

    @Test
    @Timeout(1)
    void shouldNotExpireUnexpirable() throws InterruptedException {
        var exp1id = exp.save(new TestExpirable(false),  clock.now().plusSeconds(10));

        clock.advance(Duration.ofSeconds(12));
        Thread.sleep(100);
        assert !repo.findById(exp1id).get().expired : "Expirable 1 must not expire";
    }
}
