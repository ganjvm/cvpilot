package dev.gan.cvpilot.exception;

import dev.gan.cvpilot.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleLimitExceededException(LimitExceededException ex) {
        log.warn("Limit exceeded for plan {}: {}", ex.getPlan(), ex.getMessage());
        Map<String, Object> details = Map.of(
                "remaining", ex.getRemaining(),
                "plan", ex.getPlan(),
                "upgradeUrl", "/api/v1/billing/upgrade"
        );
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(new ApiResponse<>("error", details,
                        new dev.gan.cvpilot.dto.ErrorDetail(ex.getErrorCode(), ex.getMessage())));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        log.warn("API exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        log.warn("Validation error: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("File upload too large: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("FILE_TOO_LARGE", "File is too large. Maximum size is 10MB."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
