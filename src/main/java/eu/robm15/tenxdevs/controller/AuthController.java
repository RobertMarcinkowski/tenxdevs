package eu.robm15.tenxdevs.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Public endpoint to check if the auth API is available
     */
    @GetMapping("/status")
    public Map<String, String> status() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Authentication API is available");
        return response;
    }

    /**
     * Protected endpoint to get current user information
     * Requires valid JWT token in Authorization header
     */
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();

        if (authentication != null && authentication.isAuthenticated()) {
            response.put("authenticated", true);
            response.put("principal", authentication.getPrincipal());
            response.put("authorities", authentication.getAuthorities());
        } else {
            response.put("authenticated", false);
        }

        return response;
    }
}
