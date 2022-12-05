package wildme.org.ratelimiter.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import wildme.org.ratelimiter.model.LoginAttempt;
import wildme.org.ratelimiter.repository.LoginAttemptRepository;

@Service
public class DefaultRateLimiterService implements IRateLimiterService {

    private final int attemptsPerIpInLastHour;
    private final int attemptsPerIpInLastMinute;
    private final int attemptsPerCookieInLastTenSeconds;
    private final int attemptsPerUsernameInLastHour;
    private final LoginAttemptRepository loginAttemptRepository;

    public DefaultRateLimiterService(
            @Value("${rate-limiter.thresholds.attempts-per-ip-in-last-hour:15}")
                    int attemptsPerIpInLastHour,
            @Value("${rate-limiter.thresholds.attempts-per-ip-in-last-minute:5}")
                    int attemptsPerIpInLastMinute,
            @Value("${rate-limiter.thresholds.attempts-per-cookie-in-last-ten-seconds:2}")
                    int attemptsPerCookieInLastTenSeconds,
            @Value("${rate-limiter.thresholds.attempts-per-username-in-last-hour:10}")
                    int attemptsPerUsernameInLastHour,
            LoginAttemptRepository loginAttemptRepository) {
        this.attemptsPerIpInLastHour = attemptsPerIpInLastHour;
        this.attemptsPerIpInLastMinute = attemptsPerIpInLastMinute;
        this.attemptsPerCookieInLastTenSeconds = attemptsPerCookieInLastTenSeconds;
        this.attemptsPerUsernameInLastHour = attemptsPerUsernameInLastHour;
        this.loginAttemptRepository = loginAttemptRepository;
    }

    @Override
    public Mono<Boolean> loginRateLimiter(@NonNull String ip, String cookieId, @NonNull String username) {
        return loginAttemptRepository.save(LoginAttempt.builder()
                        .ip(ip)
                        .cookieId(cookieId)
                        .username(username)
                        .build())
                .map(LoginAttempt::getCreatedAt)
                .flatMap(now -> Mono.zip(
                                List.of(
                                        checkByIp(ip, now),
                                        checkByCookieId(cookieId, now),
                                        checkByUsername(username, now)
                                ),
                                results -> Arrays.stream(results)
                                        .map(result -> (Boolean) result)
                                        .reduce(true, (a, b) -> a && b)
                        )
                );
    }

    protected Mono<Boolean> checkByIp(String ip, LocalDateTime now) {
        var lastHour = now.minusHours(1);
        var lastMinute = now.minusMinutes(1);
        return loginAttemptRepository.findAllByIpAndCreatedAtAfter(ip, lastHour)
                .collectList()
                .map(results -> {
                    var countInLastHour = results.size();
                    var countInLastMinute = results.stream()
                            .filter(loginAttempt -> loginAttempt.getCreatedAt().isAfter(lastMinute))
                            .count();
                    return countInLastMinute <= attemptsPerIpInLastMinute && countInLastHour <= attemptsPerIpInLastHour;
                });
    }

    protected Mono<Boolean> checkByCookieId(String cookieId, LocalDateTime now) {
        if (cookieId == null) {
            return Mono.just(true);
        }
        var lastTenSeconds = now.minusSeconds(10);
        return loginAttemptRepository.findAllByCookieIdAndCreatedAtAfter(cookieId, lastTenSeconds)
                .collectList()
                .map(results -> results.size() <= attemptsPerCookieInLastTenSeconds);
    }

    protected Mono<Boolean> checkByUsername(String username, LocalDateTime now) {
        var lastHour = now.minusHours(1);
        return loginAttemptRepository.findAllByUsernameAndCreatedAtAfter(username, lastHour)
                .collectList()
                .map(results -> results.size() <= attemptsPerUsernameInLastHour);
    }
}
