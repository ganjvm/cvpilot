package dev.gan.cvpilot.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApiException {

    public AuthenticationException(String message) {
        super(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_ERROR", message);
    }
}
