package eu.robm15.tenxdevs.service;

import eu.robm15.tenxdevs.model.Note;
import eu.robm15.tenxdevs.model.TravelPreferences;
import eu.robm15.tenxdevs.model.TripPlan;
import eu.robm15.tenxdevs.repository.TripPlanRepository;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for generating and managing trip plans using AI.
 */
@Service
public class TripPlanService {

    @Autowired
    private TripPlanRepository tripPlanRepository;

    @Autowired
    private TravelPreferencesService preferencesService;

    @Autowired
    private AIUsageLimitService aiUsageLimitService;

    @Autowired(required = false) // Optional for localh2 profile without AI configured
    private OpenAiChatModel openAiChatModel;

    /**
     * Check if user has sufficient preferences filled (at least 3 categories)
     *
     * @param userId Supabase user ID
     * @return true if user has at least 3 preferences filled
     */
    public boolean hasMinimumPreferences(String userId) {
        Optional<TravelPreferences> prefsOpt = preferencesService.getPreferencesByUserId(userId);

        if (prefsOpt.isEmpty()) {
            return false;
        }

        TravelPreferences prefs = prefsOpt.get();
        int filledCount = 0;

        if (prefs.getBudget() != null) filledCount++;
        if (prefs.getPace() != null) filledCount++;
        if (prefs.getInterests() != null && !prefs.getInterests().isEmpty()) filledCount++;
        if (prefs.getAccommodationStyle() != null) filledCount++;
        if (prefs.getTransport() != null && !prefs.getTransport().isEmpty()) filledCount++;
        if (prefs.getFoodPreferences() != null && !prefs.getFoodPreferences().isEmpty()) filledCount++;
        if (prefs.getSeason() != null) filledCount++;

        return filledCount >= 3;
    }

    /**
     * Generate a trip plan using AI based on note content and user preferences
     *
     * @param userId Supabase user ID
     * @param note   The note to base the plan on
     * @return Generated TripPlan
     * @throws IllegalStateException if user doesn't have minimum preferences or exceeded AI limit
     * @throws RuntimeException      if AI generation fails
     */
    @Transactional
    public TripPlan generatePlan(String userId, Note note) {
        // Validate minimum preferences
        if (!hasMinimumPreferences(userId)) {
            throw new IllegalStateException("User must have at least 3 preferences filled to generate a plan");
        }

        // Check AI usage limit
        if (!aiUsageLimitService.canGeneratePlan(userId)) {
            throw new IllegalStateException("Daily AI usage limit exceeded. Limit: "
                + aiUsageLimitService.getDailyLimit() + " plans per day");
        }

        // Get user preferences
        TravelPreferences prefs = preferencesService.getPreferencesByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("Preferences not found"));

        // Build AI prompt
        String prompt = buildTripPlanPrompt(note, prefs);

        // Call AI to generate plan
        String generatedContent;
        try {
            if (openAiChatModel == null) {
                // Fallback for localh2 profile without AI configured
                generatedContent = "Mock AI-generated plan for testing:\n\n" +
                    "Day 1:\n- Morning: Arrival and hotel check-in\n- Afternoon: City tour\n- Evening: Local restaurant\n\n" +
                    "Day 2:\n- Morning: Museum visit\n- Afternoon: Shopping\n- Evening: Sunset viewpoint\n\n" +
                    "Day 3:\n- Morning: Nature excursion\n- Afternoon: Beach relaxation\n- Evening: Departure";
            } else {
                generatedContent = openAiChatModel.call(prompt);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate trip plan: " + e.getMessage(), e);
        }

        // Create and save trip plan
        TripPlan tripPlan = new TripPlan(userId, note.getId(), generatedContent);
        return tripPlanRepository.save(tripPlan);
    }

    /**
     * Build a comprehensive prompt for the AI model
     */
    private String buildTripPlanPrompt(Note note, TravelPreferences prefs) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a professional travel planner. Generate a detailed trip plan based on the following information:\n\n");

        // Note content
        prompt.append("Trip Note:\n");
        prompt.append("Title: ").append(note.getTitle()).append("\n");
        prompt.append("Description: ").append(note.getContent()).append("\n\n");

        // User preferences
        prompt.append("Traveler Preferences:\n");

        if (prefs.getBudget() != null) {
            prompt.append("- Budget: ").append(prefs.getBudget().getDisplayName()).append("\n");
        }

        if (prefs.getPace() != null) {
            prompt.append("- Travel Pace: ").append(prefs.getPace().getDisplayName()).append("\n");
        }

        if (prefs.getInterests() != null && !prefs.getInterests().isEmpty()) {
            prompt.append("- Interests: ");
            prefs.getInterests().forEach(interest ->
                prompt.append(interest.getDisplayName()).append(", "));
            prompt.setLength(prompt.length() - 2); // Remove last comma
            prompt.append("\n");
        }

        if (prefs.getAccommodationStyle() != null) {
            prompt.append("- Accommodation Style: ").append(prefs.getAccommodationStyle().getDisplayName()).append("\n");
        }

        if (prefs.getTransport() != null && !prefs.getTransport().isEmpty()) {
            prompt.append("- Preferred Transport: ");
            prefs.getTransport().forEach(transport ->
                prompt.append(transport.getDisplayName()).append(", "));
            prompt.setLength(prompt.length() - 2); // Remove last comma
            prompt.append("\n");
        }

        if (prefs.getFoodPreferences() != null && !prefs.getFoodPreferences().isEmpty()) {
            prompt.append("- Food Preferences: ");
            prefs.getFoodPreferences().forEach(food ->
                prompt.append(food.getDisplayName()).append(", "));
            prompt.setLength(prompt.length() - 2); // Remove last comma
            prompt.append("\n");
        }

        if (prefs.getSeason() != null) {
            prompt.append("- Preferred Season: ").append(prefs.getSeason().getDisplayName()).append("\n");
        }

        prompt.append("\n");
        prompt.append("Please generate a day-by-day trip plan with specific attractions and activities. ");
        prompt.append("Format the plan as a clear list organized by days. ");
        prompt.append("Include morning, afternoon, and evening activities for each day. ");
        prompt.append("Make sure recommendations align with the traveler's preferences and budget.");

        return prompt.toString();
    }

    /**
     * Get all trip plans for a specific note
     */
    public List<TripPlan> getPlansByNoteId(Long noteId) {
        return tripPlanRepository.findByNoteId(noteId);
    }

    /**
     * Get all trip plans for a user
     */
    public List<TripPlan> getPlansByUserId(String userId) {
        return tripPlanRepository.findByUserId(userId);
    }

    /**
     * Get a specific trip plan by ID
     */
    public Optional<TripPlan> getPlanById(Long id) {
        return tripPlanRepository.findById(id);
    }

    /**
     * Rate a trip plan (US-010)
     *
     * @param planId Plan ID
     * @param userId User ID (for ownership validation)
     * @param rating Rating (1-5)
     * @return Updated TripPlan
     */
    @Transactional
    public Optional<TripPlan> ratePlan(Long planId, String userId, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Optional<TripPlan> planOpt = tripPlanRepository.findById(planId);

        if (planOpt.isEmpty()) {
            return Optional.empty();
        }

        TripPlan plan = planOpt.get();

        // Validate ownership
        if (!plan.getUserId().equals(userId)) {
            throw new IllegalStateException("Cannot rate plan owned by another user");
        }

        plan.setRating(rating);
        return Optional.of(tripPlanRepository.save(plan));
    }

    /**
     * Delete a trip plan
     */
    @Transactional
    public boolean deletePlan(Long planId, String userId) {
        Optional<TripPlan> planOpt = tripPlanRepository.findById(planId);

        if (planOpt.isEmpty()) {
            return false;
        }

        TripPlan plan = planOpt.get();

        // Validate ownership
        if (!plan.getUserId().equals(userId)) {
            throw new IllegalStateException("Cannot delete plan owned by another user");
        }

        tripPlanRepository.delete(plan);
        return true;
    }
}
