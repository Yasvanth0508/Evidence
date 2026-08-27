package com.example.backend.pipeline.feature.client;

import com.example.backend.pipeline.feature.config.MistralAiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MistralAiClient {

    private static final Logger log = LoggerFactory.getLogger(MistralAiClient.class);

    private final MistralAiConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MistralAiClient(MistralAiConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Calls Mistral AI Chat Completion API with JSON format enforcement.
     */
    public String generateChatCompletion(String systemPrompt, String userPrompt) throws Exception {
        if (!config.isConfigured()) {
            throw new IllegalStateException("Mistral AI API key is not configured in application.properties");
        }

        String endpoint = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        log.info("Sending request to Mistral AI endpoint: {} with model: {}", endpoint, config.getModel());

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("model", config.getModel());
        requestPayload.put("temperature", 0.2);
        requestPayload.put("response_format", Map.of("type", "json_object"));

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
        requestPayload.put("messages", messages);

        String requestBodyJson = objectMapper.writeValueAsString(requestPayload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey().trim())
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
            return response.body();
        } else {
            log.error("Mistral AI API call failed with status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Mistral AI API returned status " + response.statusCode() + ": " + response.body());
        }
    }
}
