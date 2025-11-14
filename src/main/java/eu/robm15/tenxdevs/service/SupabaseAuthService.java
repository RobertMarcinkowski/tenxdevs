package eu.robm15.tenxdevs.service;

import eu.robm15.tenxdevs.config.SupabaseConfigProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Profile("!localh2") // Not active for localh2 profile
public class SupabaseAuthService {

    private final SupabaseConfigProperties supabaseConfig;
    private final RestTemplate restTemplate;

    public SupabaseAuthService(SupabaseConfigProperties supabaseConfig) {
        this.supabaseConfig = supabaseConfig;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send password reset email to user via Supabase API
     * @param email User's email address
     * @param redirectUrl URL to redirect user after clicking reset link (optional)
     * @return true if successful, false otherwise
     */
    public boolean sendPasswordResetEmail(String email, String redirectUrl) {
        try {
            String url = supabaseConfig.getUrl() + "/auth/v1/recover";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseConfig.getAnonKey());

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", email);
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                requestBody.put("redirect_to", redirectUrl);
            }

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            // Log error but don't expose details for security
            System.err.println("Password reset email failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user password using reset token
     * @param accessToken The password reset token from email link
     * @param newPassword The new password
     * @return true if successful, false otherwise
     */
    public boolean updatePassword(String accessToken, String newPassword) {
        try {
            String url = supabaseConfig.getUrl() + "/auth/v1/user";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseConfig.getAnonKey());
            headers.set("Authorization", "Bearer " + accessToken);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("password", newPassword);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                String.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("Password update failed: " + e.getMessage());
            return false;
        }
    }
}
