package com.example.backend.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenVerifier {

    private final ObjectMapper objectMapper;

    @Value("${google.client-id:}")
    private String expectedClientId;

    public record GoogleProfile(String email, String name, String sub, String picture) {}

    public GoogleProfile verifyGoogleIdToken(String tokenString) {
        if (tokenString == null || tokenString.isBlank()) {
            return null;
        }

        String token = tokenString.trim();

        // 1. If token is in JWT format (header.payload.signature), verify as ID token
        if (token.contains(".") && token.split("\\.").length >= 3) {
            GoogleProfile profile = verifyAsIdToken(token);
            if (profile != null) {
                return profile;
            }
        }

        // 2. Otherwise (or if ID token verification was not applicable), verify as OAuth2 Access Token
        GoogleProfile accessProfile = verifyAsAccessToken(token);
        if (accessProfile != null) {
            return accessProfile;
        }

        // 3. Fallback: try ID token endpoint in case format was non-standard
        return verifyAsIdToken(token);
    }

    private GoogleProfile verifyAsIdToken(String idToken) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());

                if (!isAudienceValid(root)) {
                    return null;
                }

                String email = root.path("email").asText(null);
                String emailVerified = root.path("email_verified").asText("true");
                if (email == null || email.isBlank() || !Boolean.parseBoolean(emailVerified)) {
                    log.warn("Google ID token email missing or unverified: email={}", email);
                    return null;
                }

                String name = root.path("name").asText(null);
                String sub = root.path("sub").asText(null);
                String picture = root.path("picture").asText(null);

                log.info("Google ID token verified successfully for: {}", email);
                return new GoogleProfile(email, name, sub, picture);
            }
        } catch (Exception e) {
            log.warn("Google ID token verification failed: {}", e.getMessage());
        }
        return null;
    }

    private GoogleProfile verifyAsAccessToken(String accessToken) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            // Check tokeninfo for audience / token validity
            HttpRequest tokenInfoReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?access_token=" + accessToken))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> tokenInfoRes = client.send(tokenInfoReq, HttpResponse.BodyHandlers.ofString());
            if (tokenInfoRes.statusCode() != 200) {
                return null;
            }

            JsonNode tokenInfo = objectMapper.readTree(tokenInfoRes.body());
            if (!isAudienceValid(tokenInfo)) {
                return null;
            }

            // Retrieve full profile from Google userinfo
            HttpRequest userInfoReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> userInfoRes = client.send(userInfoReq, HttpResponse.BodyHandlers.ofString());
            if (userInfoRes.statusCode() == 200) {
                JsonNode userRoot = objectMapper.readTree(userInfoRes.body());

                String email = userRoot.path("email").asText(tokenInfo.path("email").asText(null));
                String emailVerified = userRoot.path("email_verified").asText("true");
                if (email == null || email.isBlank() || !Boolean.parseBoolean(emailVerified)) {
                    log.warn("Google access token email missing or unverified: email={}", email);
                    return null;
                }

                String name = userRoot.path("name").asText(null);
                String sub = userRoot.path("sub").asText(null);
                String picture = userRoot.path("picture").asText(null);

                log.info("Google access token verified successfully for: {}", email);
                return new GoogleProfile(email, name, sub, picture);
            }
        } catch (Exception e) {
            log.warn("Google access token verification failed: {}", e.getMessage());
        }
        return null;
    }

    private boolean isAudienceValid(JsonNode root) {
        if (expectedClientId != null && !expectedClientId.isBlank()) {
            String aud = root.path("aud").asText("");
            String azp = root.path("azp").asText("");
            if (!expectedClientId.equals(aud) && !expectedClientId.equals(azp)) {
                log.warn("Google token audience mismatch! Expected [{}], received aud=[{}], azp=[{}]",
                        expectedClientId, aud, azp);
                return false;
            }
        }
        return true;
    }
}
