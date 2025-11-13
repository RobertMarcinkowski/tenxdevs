package eu.robm15.tenxdevs.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock authentication controller for localh2 profile
 * Simulates Supabase authentication without requiring actual Supabase infrastructure
 */
@RestController
@Profile("localh2")
@RequestMapping("/api/mock-auth")
public class MockAuthController {

    // Simple in-memory user store for demo purposes
    private static final Map<String, String> MOCK_USERS = new HashMap<>();
    private static final Map<String, MockUser> MOCK_SESSIONS = new HashMap<>();

    static {
        // Pre-configured mock users
        MOCK_USERS.put("user@test.com", "password");
        MOCK_USERS.put("admin@test.com", "admin");
        MOCK_USERS.put("dev@test.com", "dev");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        // Validate credentials
        if (!MOCK_USERS.containsKey(email) || !MOCK_USERS.get(email).equals(password)) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "Invalid credentials",
                "message", "Email or password is incorrect"
            ));
        }

        // Create mock session
        String userId = UUID.nameUUIDFromBytes(email.getBytes()).toString();
        String accessToken = generateMockToken(userId, email);

        MockUser user = new MockUser(userId, email, "authenticated");
        MOCK_SESSIONS.put(accessToken, user);

        Map<String, Object> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("user", Map.of(
            "id", userId,
            "email", email,
            "role", "authenticated"
        ));
        response.put("session", Map.of(
            "access_token", accessToken,
            "user", Map.of(
                "id", userId,
                "email", email,
                "role", "authenticated"
            )
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        // Check if user already exists
        if (MOCK_USERS.containsKey(email)) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "User already exists",
                "message", "A user with this email already exists"
            ));
        }

        // Register new user
        MOCK_USERS.put(email, password);

        // Create session for new user
        String userId = UUID.nameUUIDFromBytes(email.getBytes()).toString();
        String accessToken = generateMockToken(userId, email);

        MockUser user = new MockUser(userId, email, "authenticated");
        MOCK_SESSIONS.put(accessToken, user);

        Map<String, Object> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("user", Map.of(
            "id", userId,
            "email", email,
            "role", "authenticated"
        ));
        response.put("session", Map.of(
            "access_token", accessToken,
            "user", Map.of(
                "id", userId,
                "email", email,
                "role", "authenticated"
            )
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            MOCK_SESSIONS.remove(token);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            MockUser user = MOCK_SESSIONS.get(token);

            if (user != null) {
                return ResponseEntity.ok(Map.of(
                    "access_token", token,
                    "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                    )
                ));
            }
        }
        return ResponseEntity.ok(Map.of("session", (Object) null));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            MockUser user = MOCK_SESSIONS.get(token);

            if (user != null) {
                return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "authenticated", true
                ));
            }
        }
        return ResponseEntity.status(401).body(Map.of(
            "error", "Not authenticated",
            "message", "No valid session found"
        ));
    }

    private String generateMockToken(String userId, String email) {
        // Create a simple mock JWT-like token (Base64 encoded JSON)
        String payload = String.format("{\"sub\":\"%s\",\"email\":\"%s\",\"role\":\"authenticated\"}", userId, email);
        return Base64.getEncoder().encodeToString(payload.getBytes());
    }

    // DTOs
    static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class RegisterRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class MockUser {
        private final String id;
        private final String email;
        private final String role;

        public MockUser(String id, String email, String role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}
