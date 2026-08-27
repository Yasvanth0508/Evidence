package com.example.backend.assessment.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProcessLogBuffer {

    private static final int MAX_LOG_LENGTH = 100_000;
    private final Map<String, StringBuilder> logBuffers = new ConcurrentHashMap<>();

    public void append(String executionKey, String text) {
        if (text == null || text.isEmpty()) return;
        logBuffers.compute(executionKey, (k, existing) -> {
            StringBuilder sb = existing != null ? existing : new StringBuilder();
            if (sb.length() + text.length() > MAX_LOG_LENGTH) {
                sb.delete(0, text.length());
            }
            sb.append(text);
            return sb;
        });
    }

    public void clear(String executionKey) {
        logBuffers.remove(executionKey);
    }

    public String getLogs(String executionKey) {
        StringBuilder sb = logBuffers.get(executionKey);
        return sb != null ? sb.toString() : "";
    }
}
