package com.example.backend.common.util;

import java.util.UUID;

public final class UUIDUtils {

    private UUIDUtils() {}

    public static UUID parseUuidOrNull(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
