package dev.gan.cvpilot.exception;

import org.springframework.http.HttpStatus;

public class LlmException extends ApiException {

    public LlmException(String message) {
        super(HttpStatus.BAD_GATEWAY, "LLM_ERROR", message);
    }
}
