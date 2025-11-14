package eu.robm15.tenxdevs.controller;

import eu.robm15.tenxdevs.model.*;
import eu.robm15.tenxdevs.service.SupabaseJwtService;
import eu.robm15.tenxdevs.service.TravelPreferencesService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/preferences")
public class PreferencesController {

    @Autowired
    private TravelPreferencesService preferencesService;

    @Autowired(required = false) // Optional for localh2 profile
    private SupabaseJwtService jwtService;

    /**
     * Get all available enum options for the preference form
     */
    @GetMapping("/options")
    public Map<String, List<Map<String, String>>> getPreferenceOptions() {
        Map<String, List<Map<String, String>>> options = new HashMap<>();

        options.put("budget", enumToList(Budget.values()));
        options.put("pace", enumToList(Pace.values()));
        options.put("interests", enumToList(Interest.values()));
        options.put("accommodationStyle", enumToList(AccommodationStyle.values()));
        options.put("transport", enumToList(Transport.values()));
        options.put("foodPreferences", enumToList(FoodPreference.values()));
        options.put("season", enumToList(Season.values()));

        return options;
    }

    /**
     * Get current user's travel preferences
     */
    @GetMapping
    public ResponseEntity<TravelPreferences> getPreferences(HttpServletRequest request) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<TravelPreferences> preferences = preferencesService.getPreferencesByUserId(userId);

        return preferences
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Save or update current user's travel preferences
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> savePreferences(
        @RequestBody TravelPreferences preferences,
        HttpServletRequest request
    ) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            TravelPreferences savedPreferences = preferencesService.savePreferences(userId, preferences);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Preferences saved successfully");
            response.put("preferences", savedPreferences);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to save preferences: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete current user's travel preferences
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deletePreferences(HttpServletRequest request) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            preferencesService.deletePreferences(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Preferences deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete preferences: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Helper method to convert enum values to a list of maps for frontend
     */
    private <E extends Enum<E>> List<Map<String, String>> enumToList(E[] values) {
        List<Map<String, String>> list = new ArrayList<>();
        for (E value : values) {
            Map<String, String> item = new HashMap<>();
            item.put("value", value.name());

            // Try to get display name if the enum has a getDisplayName method
            try {
                String displayName = (String) value.getClass()
                    .getMethod("getDisplayName")
                    .invoke(value);
                item.put("label", displayName);
            } catch (Exception e) {
                // Fallback to enum name
                item.put("label", value.name().replace("_", " "));
            }

            list.add(item);
        }
        return list;
    }

    /**
     * Extract user ID from JWT token in the request
     */
    private String extractUserId(HttpServletRequest request) {
        // For localh2 profile (mock auth), return a default user ID
        if (jwtService == null) {
            return "mock-user-id";
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.extractSubject(token);
        }

        return null;
    }
}
