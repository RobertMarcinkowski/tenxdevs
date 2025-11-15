package eu.robm15.tenxdevs.config;

import eu.robm15.tenxdevs.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Security configuration for localh2 profile - NO AUTHENTICATION
     * All endpoints are accessible without authentication for local development convenience
     */
    @Configuration
    @Profile("localh2")
    public static class LocalH2SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                    .anyRequest().permitAll() // All endpoints accessible without authentication
                )
                .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin()) // For H2 console
                );

            return http.build();
        }
    }

    /**
     * Security configuration for all other profiles - JWT AUTHENTICATION REQUIRED
     * Uses Supabase JWT tokens for authentication
     */
    @Configuration
    @Profile("!localh2")
    public static class JwtSecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public JwtSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
            this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless JWT authentication
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No sessions
                )
                .authorizeHttpRequests(authorize -> authorize
                    // Public endpoints - no authentication required
                    .requestMatchers("/", "/landing", "/login", "/register", "/app", "/profile", "/h2-console/**").permitAll()
                    .requestMatchers("/forgot-password", "/reset-password").permitAll() // Password reset views
                    .requestMatchers("/api/auth/status").permitAll() // Public auth status
                    .requestMatchers("/api/auth/reset-password-request", "/api/auth/update-password").permitAll() // Password reset endpoints
                    .requestMatchers("/api/auth/me").authenticated() // Protected user info endpoint
                    // Protected endpoints - JWT authentication required
                    .requestMatchers("/api/preferences/**").authenticated() // Travel preferences endpoints
                    .requestMatchers("/api/notes/**").authenticated() // Notes endpoints
                    .requestMatchers("/api/trip-plans/**").authenticated() // Trip plan endpoints
                    .requestMatchers("/api/protected/**").authenticated()
                    .anyRequest().authenticated() // All other requests require authentication
                )
                .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin()) // For H2 console
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }
    }
}
