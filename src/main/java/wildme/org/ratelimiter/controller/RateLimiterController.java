package wildme.org.ratelimiter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import wildme.org.ratelimiter.controller.request.LoginAttemptRequest;
import wildme.org.ratelimiter.service.IRateLimiterService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rate-limiter")
public class RateLimiterController {

    private final IRateLimiterService rateLimiterService;

    @PostMapping("/login")
    public Mono<ResponseEntity> loginRateLimiter(@Valid @RequestBody LoginAttemptRequest request) {
        return rateLimiterService.loginRateLimiter(request.getIp(), request.getCookieId(), request.getUsername())
                .map(result -> result ?
                        ResponseEntity.ok().build()
                        : ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
    }
}
