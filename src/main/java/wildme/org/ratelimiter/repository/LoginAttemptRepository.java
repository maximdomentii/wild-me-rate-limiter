package wildme.org.ratelimiter.repository;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import wildme.org.ratelimiter.model.LoginAttempt;

@Repository
public interface LoginAttemptRepository extends R2dbcRepository<LoginAttempt, UUID> {

    Flux<LoginAttempt> findAllByIpAndCreatedAtAfter(String ip, LocalDateTime localDateTime);
    Flux<LoginAttempt> findAllByCookieIdAndCreatedAtAfter(String cookieId, LocalDateTime localDateTime);
    Flux<LoginAttempt> findAllByUsernameAndCreatedAtAfter(String username, LocalDateTime localDateTime);
}
