package dev.gan.cvpilot.service;

import dev.gan.cvpilot.dto.auth.AuthResponse;
import dev.gan.cvpilot.entity.User;
import dev.gan.cvpilot.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final GoogleTokenService googleTokenService;
    private final JwtService jwtService;

    public AuthResponse authenticateWithGoogle(String googleAccessToken) {
        GoogleTokenService.GoogleUserInfo googleUserInfo = googleTokenService.verifyAccessToken(googleAccessToken);

        User user = userService.findByGoogleId(googleUserInfo.googleId())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(googleUserInfo.email().toLowerCase())
                            .googleId(googleUserInfo.googleId())
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    User saved = userService.save(newUser);
                    log.info("New user registered via Google: {}", googleUserInfo.email());
                    return saved;
                });

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiration() / 1000);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isValidToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token.");
        }

        if (!"refresh".equals(jwtService.getTokenType(refreshToken))) {
            throw new AuthenticationException("Invalid token type.");
        }

        var userId = jwtService.extractUserId(refreshToken);
        User user = userService.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found."));

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        return new AuthResponse(newAccessToken, newRefreshToken, jwtService.getAccessTokenExpiration() / 1000);
    }
}
