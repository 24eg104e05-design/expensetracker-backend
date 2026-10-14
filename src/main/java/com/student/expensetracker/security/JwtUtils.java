package com.student.expensetracker.security;

import com.student.expensetracker.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Generates, validates and extracts claims from the HMAC-SHA256 JWT (PRD 3.3). */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final SecretKey key;
    private final long expirationMs;
    private final String issuer;

    public JwtUtils(JwtProperties properties) {
        byte[] secret = properties.secret() == null ? new byte[0] : properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least 256 bits (32 bytes) for HMAC-SHA256; got " + secret.length + " bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.expirationMs = properties.expiration();
        this.issuer = properties.issuer();
    }

    public String generateToken(Long userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                // Pinned explicitly: JJWT would otherwise pick HS384/HS512 based on
                // secret length, but PRD 4.1 mandates HMAC-SHA256.
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** Returns the subject (email), or null when the token is absent/invalid/expired. */
    public String extractSubject(String token) {
        return parse(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object uid = parse(token).get("uid");
        return uid instanceof Number n ? n.longValue() : null;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            throw new InvalidTokenException(ex.getMessage(), ex);
        }
    }
}