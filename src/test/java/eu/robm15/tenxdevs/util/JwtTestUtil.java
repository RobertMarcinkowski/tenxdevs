package eu.robm15.tenxdevs.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTestUtil {

    /**
     * Generates a test JWT token with the given secret
     */
    public static String generateTestToken(String secret, String userId, String email) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", "authenticated");

        return Jwts.builder()
            .subject(userId)
            .claims(claims)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .signWith(key)
            .compact();
    }

    /**
     * Generates a test JWT token with default values
     */
    public static String generateTestToken(String secret) {
        return generateTestToken(secret, "test-user-id", "test@example.com");
    }
}
