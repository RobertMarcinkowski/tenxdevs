package eu.robm15.tenxdevs.security;

import eu.robm15.tenxdevs.service.SupabaseJwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Profile("!localh2") // Not active for localh2 profile
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SupabaseJwtService jwtService;

    public JwtAuthenticationFilter(SupabaseJwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix

            try {
                // Validate token and get authentication
                Authentication authentication = jwtService.validateToken(token);

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Log the error but don't stop the filter chain
                logger.debug("JWT validation failed: " + e.getMessage());
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
