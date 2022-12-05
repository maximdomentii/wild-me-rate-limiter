# Wild Me Code Challenge

## Rate Limiter

### Implementation
Implemented with Java 17, Spring Boot 3, Gradle and H2 In-Mem database as a microservice.

The loginRateLimiter login is in the DefaultRateLimiterService class which has unit tests for all provided use-cases.

I'm using an In-Mem DB to store the state for rate limiter logic that can be easily  replaces with a proper production DB.
I have added a rest controller that you can use test the logic. The controller is also covered with integration tests for all provided use-cases.

### Build, test and run
```bash
# To build the project run from project root folder
./gradlew clean build

# For tests:
./gradlew clean test -info

# To start the service:
./gradlew bootRun

# Make a request with:
curl -X POST -H "Content-Type: application/json" -d '{"ip":"some_ip", "username":"some_username", "cookieId": "some_cookie_id"}' http://localhost:8080/rate-limiter/login
```