package dev.gan.cvpilot.exception;

import org.springframework.http.HttpStatus;

public class RateLimitException extends ApiException {

    public RateLimitException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_ERROR", message);
    }
}
