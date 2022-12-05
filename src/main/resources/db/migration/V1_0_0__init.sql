CREATE TABLE IF NOT EXISTS LOGIN_ATTEMPT (
    id         uuid default RANDOM_UUID(),
    ip         text,
    cookie_id  text,
    username   text,
    version    int,
    created_at timestamp with time zone,
    PRIMARY KEY(id)
);

CREATE INDEX loginAttemptIpIndex on LOGIN_ATTEMPT(ip, created_at);
CREATE INDEX loginAttemptCookieIdIndex on LOGIN_ATTEMPT(cookie_id, created_at);
CREATE INDEX loginAttemptUsernameIndex on LOGIN_ATTEMPT(username, created_at);