package wildme.org.ratelimiter.service;

import reactor.core.publisher.Mono;

public interface IRateLimiterService {
    Mono<Boolean> loginRateLimiter(String ip, String cookieId, String username);
}
