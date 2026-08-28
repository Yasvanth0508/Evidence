package com.example.backend.pipeline.feature.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;

@Configuration
public class MistralAiConfig {

    @Value("${mistral.ai.api-key:${MISTRAL_API_KEY:}}")
    private String apiKey;

    @Value("${mistral.ai.base-url:${MISTRAL_BASE_URL:https://api.mistral.ai/v1}}")
    private String baseUrl = "https://api.mistral.ai/v1";

    @Value("${mistral.ai.model:${MISTRAL_MODEL:mistral-small-latest}}")
    private String model = "mistral-small-latest";

    @Value("${mistral.ai.timeout-seconds:60}")
    private int timeoutSeconds = 60;

    /**
     * Initializes configuration fallbacks if environment variables or properties
     * were not directly injected by Spring.
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        // 1. Check OS Environment variable if not set
        if (this.apiKey == null || this.apiKey.trim().isEmpty() || this.apiKey.startsWith("${")) {
            this.apiKey = System.getenv("MISTRAL_API_KEY");
        }

        // 2. Check local .env file in root or backend folder
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            this.apiKey = loadFromDotEnv("MISTRAL_API_KEY");
        }

        // 3. Fallback discovery from application.properties
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
                    this.apiKey = resolvePlaceholder(props.getProperty("mistral.ai.api-key"), "");
                }
                if (this.baseUrl == null || this.baseUrl.startsWith("${")) {
                    this.baseUrl = resolvePlaceholder(props.getProperty("mistral.ai.base-url"), "https://api.mistral.ai/v1");
                }
                if (this.model == null || this.model.startsWith("${")) {
                    this.model = resolvePlaceholder(props.getProperty("mistral.ai.model"), "mistral-small-latest");
                }
                String timeoutStr = resolvePlaceholder(props.getProperty("mistral.ai.timeout-seconds"), "60");
                try {
                    this.timeoutSeconds = Integer.parseInt(timeoutStr);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {
        }

        if (this.baseUrl == null || this.baseUrl.startsWith("${") || this.baseUrl.trim().isEmpty()) {
            this.baseUrl = "https://api.mistral.ai/v1";
        }
        if (this.model == null || this.model.startsWith("${") || this.model.trim().isEmpty()) {
            this.model = "mistral-small-latest";
        }
    }

    private String loadFromDotEnv(String key) {
        String[] possiblePaths = {
                ".env",
                "backend/.env",
                "../.env",
                System.getProperty("user.dir") + "/.env",
                System.getProperty("user.dir") + "/backend/.env"
        };
        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("#") || line.isEmpty()) continue;
                        if (line.startsWith(key + "=")) {
                            String val = line.substring(key.length() + 1).trim();
                            if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                                val = val.substring(1, val.length() - 1);
                            }
                            return val;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private String resolvePlaceholder(String val, String fallback) {
        if (val == null || val.trim().isEmpty()) return fallback;
        String trimmed = val.trim();
        if (trimmed.startsWith("${") && trimmed.contains("}")) {
            int colon = trimmed.indexOf(":");
            String def = colon > 0 ? trimmed.substring(colon + 1, trimmed.length() - 1) : fallback;
            String envName = colon > 0 ? trimmed.substring(2, colon) : trimmed.substring(2, trimmed.length() - 1);
            String envVal = System.getenv(envName);
            if (envVal == null || envVal.trim().isEmpty()) {
                envVal = loadFromDotEnv(envName);
            }
            return (envVal != null && !envVal.trim().isEmpty()) ? envVal.trim() : def.trim();
        }
        return trimmed;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("your_mistral_api_key");
    }
}
