package pl.kocmon.booking.ReservationService.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import pl.kocmon.booking.ReservationService.models.Expirable;

@NoRepositoryBean
public interface ExpirableRepository<T extends Expirable> extends CrudRepository<T, Long> {
    Iterable<T> findByExpiresAtNotNull();

    long deleteByIdAndExpireIdempotencyKey(long id, long expireIdempotencyKey);
}
