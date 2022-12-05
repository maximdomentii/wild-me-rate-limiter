package wildme.org.ratelimiter.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wildme.org.ratelimiter.model.LoginAttempt;
import wildme.org.ratelimiter.repository.LoginAttemptRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRateLimiterServiceTest {

    private final int attemptsPerIpInLastHour = 15;
    private final int attemptsPerIpInLastMinute = 5;
    private final int attemptsPerCookieInLastTenSeconds = 2;
    private final int attemptsPerUsernameInLastHour = 10;
    @Mock
    private LoginAttemptRepository repository;
    private DefaultRateLimiterService service;

    @BeforeEach
    void setUp() {
        service = new DefaultRateLimiterService(attemptsPerIpInLastHour, attemptsPerIpInLastMinute,
                attemptsPerCookieInLastTenSeconds, attemptsPerUsernameInLastHour, repository);
    }

    @Test
    void loginRateLimiter_allConditionsReturnTrue_returnTrue() {
        var spyService = spy(service);
        doReturn(Mono.just(true)).when(spyService).checkByIp(any(), any());
        doReturn(Mono.just(true)).when(spyService).checkByCookieId(any(), any());
        doReturn(Mono.just(true)).when(spyService).checkByUsername(any(), any());
        when(repository.save(any()))
                .thenReturn(Mono.just(LoginAttempt.builder().createdAt(LocalDateTime.now()).build()));

        StepVerifier.create(spyService.loginRateLimiter("ip", "cookie", "username"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void loginRateLimiter_checkByIpConditionsReturnFalse_returnFalse() {
        var spyService = spy(service);
        doReturn(Mono.just(false)).when(spyService).checkByIp(any(), any());
        doReturn(Mono.just(true)).when(spyService).checkByCookieId(any(), any());
        doReturn(Mono.just(true)).when(spyService).checkByUsername(any(), any());
        when(repository.save(any()))
                .thenReturn(Mono.just(LoginAttempt.builder().createdAt(LocalDateTime.now()).build()));

        StepVerifier.create(spyService.loginRateLimiter("ip", "cookie", "username"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void loginRateLimiter_checkByCookieIdConditionsReturnFalse_returnFalse() {
        var spyService = spy(service);
        doReturn(Mono.just(true)).when(spyService).checkByIp(any(), any());
        doReturn(Mono.just(false)).when(spyService).checkByCookieId(any(), any());
        doReturn(Mono.just(true)).when(spyService).checkByUsername(any(), any());
        when(repository.save(any()))
                .thenReturn(Mono.just(LoginAttempt.builder().createdAt(LocalDateTime.now()).build()));

        StepVerifier.create(spyService.loginRateLimiter("ip", "cookie", "username"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void loginRateLimiter_checkByUsernameConditionsReturnFalse_returnFalse() {
        var spyService = spy(service);
        doReturn(Mono.just(true)).when(spyService).checkByIp(any(), any());
        doReturn(Mono.just(true)).when(spyService).checkByCookieId(any(), any());
        doReturn(Mono.just(false)).when(spyService).checkByUsername(any(), any());
        when(repository.save(any()))
                .thenReturn(Mono.just(LoginAttempt.builder().createdAt(LocalDateTime.now()).build()));

        StepVerifier.create(spyService.loginRateLimiter("ip", "cookie", "username"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void checkByIp_whenMoreThanThresholdInLastHour_returnFalse() {
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i <= attemptsPerIpInLastHour; i++){
            attempts.add(LoginAttempt.builder().createdAt(LocalDateTime.now().minusMinutes(30)).build());
        }
        when(repository.findAllByIpAndCreatedAtAfter(any(), any())).thenReturn(Flux.fromIterable(attempts));

        StepVerifier.create(service.checkByIp("ip", LocalDateTime.now()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void checkByIp_whenMoreThanThresholdInLastMinute_returnFalse() {
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i <= attemptsPerIpInLastMinute; i++){
            attempts.add(LoginAttempt.builder().createdAt(LocalDateTime.now().minusSeconds(1)).build());
        }
        when(repository.findAllByIpAndCreatedAtAfter(any(), any())).thenReturn(Flux.fromIterable(attempts));

        StepVerifier.create(service.checkByIp("ip", LocalDateTime.now()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void checkByIp_whenNoMoreThanThreshold_returnTrue() {
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i < attemptsPerIpInLastHour; i++){
            attempts.add(LoginAttempt.builder().createdAt(LocalDateTime.now().minusMinutes(30)).build());
        }
        when(repository.findAllByIpAndCreatedAtAfter(any(), any())).thenReturn(Flux.fromIterable(attempts));

        StepVerifier.create(service.checkByIp("ip", LocalDateTime.now()))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void checkByCookieId_whenMoreThanThresholdInLastTenSeconds_returnFalse() {
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i <= attemptsPerCookieInLastTenSeconds; i++){
            attempts.add(LoginAttempt.builder().build());
        }
        when(repository.findAllByCookieIdAndCreatedAtAfter(any(), any())).thenReturn(Flux.fromIterable(attempts));

        StepVerifier.create(service.checkByCookieId("cookie", LocalDateTime.now()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void checkByCookieId_whenNoCookieId_returnTrue() {
        StepVerifier.create(service.checkByCookieId(null, LocalDateTime.now()))
                .expectNext(true)
                .verifyComplete();

        verify(repository, times(0)).findAllByCookieIdAndCreatedAtAfter(any(), any());
    }

    @Test
    void checkByCookieId_whenNoMoreThanThresholdInLastTenSeconds_returnTrue() {
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i < attemptsPerCookieInLastTenSeconds; i++){
            attempts.add(LoginAttempt.builder().build());
        }
        when(repository.findAllByCookieIdAndCreatedAtAfter(any(), any())).thenReturn(Flux.fromIterable(attempts));

        StepVerifier.create(service.checkByCookieId("cookie", LocalDateTime.now()))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void checkByUsername_whenMoreThanThresholdInLastHour_returnFalse() {
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i <= attemptsPerUsernameInLastHour; i++){
            attempts.add(LoginAttempt.builder().build());
        }
        when(repository.findAllByUsernameAndCreatedAtAfter(any(), any())).thenReturn(Flux.fromIterable(attempts));

        StepVerifier.create(service.checkByUsername("username", LocalDateTime.now()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void checkByUsername_whenNoMoreThanThresholdInLastHour_returnTrue() {
        var attempts = new ArrayList<LoginAttempt>();
        for (int i = 0; i < attemptsPerUsernameInLastHour; i++){
            attempts.add(LoginAttempt.builder().build());
        }
        when(repository.findAllByUsernameAndCreatedAtAfter(any(), any())).thenReturn(Flux.fromIterable(attempts));

        StepVerifier.create(service.checkByUsername("username", LocalDateTime.now()))
                .expectNext(true)
                .verifyComplete();
    }
}