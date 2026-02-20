package dev.gan.cvpilot.controller;

import dev.gan.cvpilot.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        String dbStatus;
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbStatus = "connected";
        } catch (Exception e) {
            dbStatus = "disconnected";
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "healthy",
                "database", dbStatus
        )));
    }
}
