package wildme.org.ratelimiter.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import wildme.org.ratelimiter.RateLimiterApplication;
import wildme.org.ratelimiter.controller.request.LoginAttemptRequest;
import wildme.org.ratelimiter.model.LoginAttempt;
import wildme.org.ratelimiter.repository.LoginAttemptRepository;

@ActiveProfiles("it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RateLimiterApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimiterControllerTest {

    @Value("${rate-limiter.thresholds.attempts-per-ip-in-last-hour:15}")
    private int attemptsPerIpInLastHour;
    @Value("${rate-limiter.thresholds.attempts-per-ip-in-last-minute:5}")
    private int attemptsPerIpInLastMinute;
    @Value("${rate-limiter.thresholds.attempts-per-cookie-in-last-ten-seconds:2}")
    private int attemptsPerCookieInLastTenSeconds;
    @Value("${rate-limiter.thresholds.attempts-per-username-in-last-hour:10}")
    private int attemptsPerUsernameInLastHour;

    @Autowired
    private WebTestClient client;
    @Autowired
    private LoginAttemptRepository repository;

    @AfterEach
    void tearDown() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();
    }

    @Test
    void loginRateLimiter_moreSameIpInLastHourThanThreshold_returns429() {
        var ip = "ip";
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i < attemptsPerIpInLastHour; i++){
            attempts.add(LoginAttempt.builder().ip(ip).createdAt(LocalDateTime.now().minusMinutes(30)).build());
        }

        StepVerifier.create(repository.saveAll(attempts).collectList())
                .expectNextMatches(x -> true)
                .verifyComplete();

        client.post()
                .uri("/rate-limiter/login")
                .bodyValue(new LoginAttemptRequest(ip, null, "username"))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void loginRateLimiter_moreSameIpInLastMinuteThanThreshold_returns429() {
        var ip = "ip";
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i < attemptsPerIpInLastMinute; i++){
            attempts.add(LoginAttempt.builder().ip(ip).build());
        }

        StepVerifier.create(repository.saveAll(attempts).collectList())
                .expectNextMatches(x -> true)
                .verifyComplete();

        client.post()
                .uri("/rate-limiter/login")
                .bodyValue(new LoginAttemptRequest(ip, null, "username"))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void loginRateLimiter_moreSameCookieInLastTenSecondsThanThreshold_returns429() {
        var cookieId = "cookie";
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i < attemptsPerCookieInLastTenSeconds; i++){
            attempts.add(LoginAttempt.builder().cookieId(cookieId).build());
        }

        StepVerifier.create(repository.saveAll(attempts).collectList())
                .expectNextMatches(x -> true)
                .verifyComplete();

        client.post()
                .uri("/rate-limiter/login")
                .bodyValue(new LoginAttemptRequest("ip", cookieId, "username"))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void loginRateLimiter_moreUsernamesInLastHourThanThreshold_returns429() {
        var username = "username";
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i < attemptsPerUsernameInLastHour; i++){
            attempts.add(LoginAttempt.builder().username(username).build());
        }

        StepVerifier.create(repository.saveAll(attempts).collectList())
                .expectNextMatches(x -> true)
                .verifyComplete();

        client.post()
                .uri("/rate-limiter/login")
                .bodyValue(new LoginAttemptRequest("ip", null, username))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void loginRateLimiter_noRateLimiterThresholdReached_returns200() {
        client.post()
                .uri("/rate-limiter/login")
                .bodyValue(new LoginAttemptRequest("ip", "cookie", "username"))
                .exchange()
                .expectStatus()
                .isOk();
    }
}