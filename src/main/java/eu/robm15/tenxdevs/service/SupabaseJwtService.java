package eu.robm15.tenxdevs.service;

import eu.robm15.tenxdevs.config.SupabaseConfigProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class SupabaseJwtService {

    private final SupabaseConfigProperties supabaseConfig;

    public SupabaseJwtService(SupabaseConfigProperties supabaseConfig) {
        this.supabaseConfig = supabaseConfig;
    }

    /**
     * Validates a Supabase JWT token and returns an Authentication object
     */
    public Authentication validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(
                supabaseConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8)
            );

            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            // Extract user information from claims
            String userId = claims.getSubject(); // Supabase uses 'sub' for user ID
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            // Create authorities based on role
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + (role != null ? role.toUpperCase() : "USER"))
            );

            // Create authentication object with user details
            // Using email as principal, or userId if email is not available
            String principal = email != null ? email : userId;

            return new UsernamePasswordAuthenticationToken(principal, null, authorities);

        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the subject (user ID) from a JWT token
     */
    public String extractSubject(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(
                supabaseConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8)
            );

            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
