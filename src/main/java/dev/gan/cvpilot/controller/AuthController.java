package dev.gan.cvpilot.controller;

import dev.gan.cvpilot.dto.ApiResponse;
import dev.gan.cvpilot.dto.auth.AuthResponse;
import dev.gan.cvpilot.dto.auth.GoogleAuthRequest;
import dev.gan.cvpilot.dto.auth.RefreshTokenRequest;
import dev.gan.cvpilot.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        AuthResponse authResponse = authService.authenticateWithGoogle(request.accessToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }
}
