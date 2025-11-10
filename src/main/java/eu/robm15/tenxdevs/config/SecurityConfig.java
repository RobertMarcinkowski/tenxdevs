package eu.robm15.tenxdevs.config;

import eu.robm15.tenxdevs.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
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
                .requestMatchers("/", "/landing", "/login", "/register", "/app", "/h2-console/**").permitAll()
                .requestMatchers("/api/config/**").permitAll() // Public config endpoints
                .requestMatchers("/api/auth/status").permitAll() // Public auth status
                .requestMatchers("/api/auth/me").authenticated() // Protected user info endpoint
                .requestMatchers("/api/status", "/tenxdevs").permitAll() // Public API endpoints
                // Protected endpoints - JWT authentication required
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
