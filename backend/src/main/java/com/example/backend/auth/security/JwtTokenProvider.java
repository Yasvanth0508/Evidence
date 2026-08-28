package com.example.backend.auth.security;

import com.example.backend.auth.entity.User;
import com.example.backend.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Utility provider for generating, signing, parsing, and validating JWT tokens.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (Exception e) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            // Pad to 256 bits if secret is shorter
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT session token for an authenticated user.
     *
     * @param user The authenticated User entity.
     * @return Signed compact JWT string.
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .claim("authProvider", user.getAuthProvider() != null ? user.getAuthProvider().name() : "LOCAL")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Validates the cryptographic signature and expiration of a JWT token string.
     *
     * @param token The compact JWT token to validate.
     * @return true if valid, false otherwise.
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token.trim());
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token has expired: {}", ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extracts the User UUID from the token subject.
     *
     * @param token Compact JWT string.
     * @return UUID of the user, or null if parsing fails.
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null || claims.getSubject() == null) {
            return null;
        }
        try {
            return UUID.fromString(claims.getSubject());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extracts the user Role enum from the token claim.
     *
     * @param token Compact JWT string.
     * @return Role enum, defaulting to CANDIDATE if not found or invalid.
     */
    public Role getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) return Role.CANDIDATE;
        String roleStr = claims.get("role", String.class);
        try {
            return roleStr != null ? Role.valueOf(roleStr) : Role.CANDIDATE;
        } catch (Exception e) {
            return Role.CANDIDATE;
        }
    }

    /**
     * Extracts the user email address from the token claim.
     *
     * @param token Compact JWT string.
     * @return Email string.
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("email", String.class) : null;
    }

    /**
     * Extracts all claims from a validated token.
     *
     * @param token Compact JWT string.
     * @return Claims instance or null if invalid.
     */
    public Claims getClaimsFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token.trim())
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
