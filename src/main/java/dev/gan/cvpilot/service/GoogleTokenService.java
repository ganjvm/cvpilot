package dev.gan.cvpilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gan.cvpilot.exception.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class GoogleTokenService {

    public record GoogleUserInfo(String googleId, String email, String name) {}

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GoogleTokenService(ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    public GoogleUserInfo verifyAccessToken(String accessToken) {
        try {
            String response = restClient.get()
                    .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

            JsonNode userInfo = objectMapper.readTree(response);

            String googleId = userInfo.get("sub").asText();
            String email = userInfo.get("email").asText();
            String name = userInfo.has("name") ? userInfo.get("name").asText() : null;

            return new GoogleUserInfo(googleId, email, name);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify Google access token", e);
            throw new AuthenticationException("Failed to authenticate with Google.");
        }
    }
}
