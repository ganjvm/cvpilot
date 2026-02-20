package dev.gan.cvpilot.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LimitExceededException extends ApiException {

    private final int remaining;
    private final String plan;

    public LimitExceededException(String plan) {
        super(HttpStatus.FORBIDDEN, "LIMIT_EXCEEDED", "Daily analysis limit exceeded");
        this.remaining = 0;
        this.plan = plan;
    }
}
