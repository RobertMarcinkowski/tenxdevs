package eu.robm15.tenxdevs.controller;

import eu.robm15.tenxdevs.service.SupabaseAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired(required = false) // Optional for localh2 profile
    private SupabaseAuthService supabaseAuthService;

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

    /**
     * Public endpoint to request password reset email
     * Sends password reset email via Supabase
     */
    @PostMapping("/reset-password-request")
    public ResponseEntity<Map<String, Object>> requestPasswordReset(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        // Check if running in localh2 profile (no auth service available)
        if (supabaseAuthService == null) {
            response.put("success", true);
            response.put("message", "Password reset is not available in local development mode");
            return ResponseEntity.ok(response);
        }

        String email = request.get("email");
        String redirectUrl = request.get("redirectUrl");

        if (email == null || email.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = supabaseAuthService.sendPasswordResetEmail(email, redirectUrl);

        // Always return success to prevent email enumeration attacks
        response.put("success", true);
        response.put("message", "If an account exists with this email, a password reset link will be sent.");
        return ResponseEntity.ok(response);
    }

    /**
     * Public endpoint to update password with reset token
     * Uses the access token from password reset email
     */
    @PostMapping("/update-password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        // Check if running in localh2 profile (no auth service available)
        if (supabaseAuthService == null) {
            response.put("success", false);
            response.put("message", "Password reset is not available in local development mode");
            return ResponseEntity.ok(response);
        }

        String accessToken = request.get("accessToken");
        String newPassword = request.get("newPassword");

        if (accessToken == null || accessToken.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Access token is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (newPassword == null || newPassword.length() < 6) {
            response.put("success", false);
            response.put("message", "Password must be at least 6 characters");
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = supabaseAuthService.updatePassword(accessToken, newPassword);

        response.put("success", success);
        response.put("message", success ? "Password updated successfully" : "Failed to update password. The reset link may have expired.");
        return ResponseEntity.ok(response);
    }
}
