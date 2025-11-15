package eu.robm15.tenxdevs.controller;

import eu.robm15.tenxdevs.model.Note;
import eu.robm15.tenxdevs.model.TripPlan;
import eu.robm15.tenxdevs.service.AIUsageLimitService;
import eu.robm15.tenxdevs.service.NoteService;
import eu.robm15.tenxdevs.service.SupabaseJwtService;
import eu.robm15.tenxdevs.service.TripPlanService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for managing trip plans.
 * Provides endpoints for generating AI-powered trip plans and rating them.
 */
@RestController
@RequestMapping("/api/trip-plans")
public class TripPlanController {

    @Autowired
    private TripPlanService tripPlanService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private AIUsageLimitService aiUsageLimitService;

    @Autowired(required = false) // Optional for localh2 profile
    private SupabaseJwtService jwtService;

    /**
     * Check if user can generate a plan for a specific note.
     * Validates:
     * - User has at least 3 preferences filled
     * - User has not exceeded AI usage limit
     *
     * @param noteId  Note ID to generate plan for
     * @param request HTTP request containing JWT token
     * @return Validation result with can_generate flag and messages
     */
    @GetMapping("/can-generate")
    public ResponseEntity<Map<String, Object>> canGeneratePlan(
            @RequestParam Long noteId,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> response = new HashMap<>();

        // Check if note exists and belongs to user
        Optional<Note> noteOpt = noteService.getNoteByIdAndUserId(noteId, userId);
        if (noteOpt.isEmpty()) {
            response.put("can_generate", false);
            response.put("reason", "Note not found");
            return ResponseEntity.ok(response);
        }

        // Check minimum preferences
        boolean hasMinPrefs = tripPlanService.hasMinimumPreferences(userId);
        if (!hasMinPrefs) {
            response.put("can_generate", false);
            response.put("reason", "You need to fill at least 3 travel preferences to generate a plan");
            response.put("missing_preferences", true);
            return ResponseEntity.ok(response);
        }

        // Check AI usage limit
        boolean canUseAI = aiUsageLimitService.canGeneratePlan(userId);
        if (!canUseAI) {
            int dailyLimit = aiUsageLimitService.getDailyLimit();
            response.put("can_generate", false);
            response.put("reason", "Daily AI usage limit exceeded (" + dailyLimit + " plans per day)");
            response.put("limit_exceeded", true);
            response.put("daily_limit", dailyLimit);
            return ResponseEntity.ok(response);
        }

        // All checks passed
        response.put("can_generate", true);
        response.put("remaining_usage", aiUsageLimitService.getRemainingUsage(userId));
        response.put("daily_limit", aiUsageLimitService.getDailyLimit());

        return ResponseEntity.ok(response);
    }

    /**
     * Generate a trip plan for a note (US-009)
     *
     * @param request HTTP request containing JWT token
     * @return Generated trip plan or error
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePlan(
            @RequestBody GeneratePlanRequest generateRequest,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate note exists and belongs to user
            Optional<Note> noteOpt = noteService.getNoteByIdAndUserId(generateRequest.getNoteId(), userId);
            if (noteOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Note not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Note note = noteOpt.get();

            // Generate plan (service validates preferences and limits)
            TripPlan tripPlan = tripPlanService.generatePlan(userId, note);

            response.put("success", true);
            response.put("message", "Trip plan generated successfully");
            response.put("trip_plan", tripPlan);
            response.put("remaining_usage", aiUsageLimitService.getRemainingUsage(userId));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalStateException e) {
            // Validation errors (insufficient preferences, limit exceeded)
            response.put("success", false);
            response.put("message", e.getMessage());

            if (e.getMessage().contains("preferences")) {
                response.put("missing_preferences", true);
            } else if (e.getMessage().contains("limit")) {
                response.put("limit_exceeded", true);
                response.put("daily_limit", aiUsageLimitService.getDailyLimit());
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            // AI generation errors
            response.put("success", false);
            response.put("message", "Failed to generate trip plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all trip plans for a specific note
     *
     * @param noteId  Note ID
     * @param request HTTP request containing JWT token
     * @return List of trip plans
     */
    @GetMapping
    public ResponseEntity<List<TripPlan>> getPlansByNote(
            @RequestParam Long noteId,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate note exists and belongs to user
        Optional<Note> noteOpt = noteService.getNoteByIdAndUserId(noteId, userId);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<TripPlan> plans = tripPlanService.getPlansByNoteId(noteId);
        return ResponseEntity.ok(plans);
    }

    /**
     * Get a specific trip plan by ID
     *
     * @param id      Plan ID
     * @param request HTTP request containing JWT token
     * @return Trip plan
     */
    @GetMapping("/{id}")
    public ResponseEntity<TripPlan> getPlanById(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<TripPlan> planOpt = tripPlanService.getPlanById(id);

        if (planOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TripPlan plan = planOpt.get();

        // Validate ownership
        if (!plan.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(plan);
    }

    /**
     * Rate a trip plan (US-010)
     *
     * @param id      Plan ID
     * @param request HTTP request containing JWT token
     * @return Updated trip plan
     */
    @PutMapping("/{id}/rate")
    public ResponseEntity<Map<String, Object>> ratePlan(
            @PathVariable Long id,
            @RequestBody RatePlanRequest rateRequest,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<TripPlan> updatedPlan = tripPlanService.ratePlan(id, userId, rateRequest.getRating());

            if (updatedPlan.isEmpty()) {
                response.put("success", false);
                response.put("message", "Trip plan not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("message", "Trip plan rated successfully");
            response.put("trip_plan", updatedPlan.get());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to rate trip plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete a trip plan
     *
     * @param id      Plan ID
     * @param request HTTP request containing JWT token
     * @return Success or error response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlan(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = tripPlanService.deletePlan(id, userId);

            if (deleted) {
                response.put("success", true);
                response.put("message", "Trip plan deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Trip plan not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete trip plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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

    /**
     * DTO for plan generation request
     */
    public static class GeneratePlanRequest {
        private Long noteId;

        public GeneratePlanRequest() {
        }

        public Long getNoteId() {
            return noteId;
        }

        public void setNoteId(Long noteId) {
            this.noteId = noteId;
        }
    }

    /**
     * DTO for plan rating request
     */
    public static class RatePlanRequest {
        private Integer rating;

        public RatePlanRequest() {
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }
    }
}
