package eu.robm15.tenxdevs.service;

import eu.robm15.tenxdevs.model.TravelPreferences;
import eu.robm15.tenxdevs.repository.TravelPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TravelPreferencesService {

    @Autowired
    private TravelPreferencesRepository travelPreferencesRepository;

    /**
     * Get travel preferences for a user
     * @param userId Supabase user ID
     * @return Optional containing TravelPreferences if found
     */
    public Optional<TravelPreferences> getPreferencesByUserId(String userId) {
        return travelPreferencesRepository.findByUserId(userId);
    }

    /**
     * Save or update travel preferences for a user
     * @param userId Supabase user ID
     * @param preferences TravelPreferences object to save
     * @return Saved TravelPreferences
     */
    @Transactional
    public TravelPreferences savePreferences(String userId, TravelPreferences preferences) {
        // Check if preferences already exist for this user
        Optional<TravelPreferences> existing = travelPreferencesRepository.findByUserId(userId);

        if (existing.isPresent()) {
            // Update existing preferences
            TravelPreferences existingPrefs = existing.get();
            existingPrefs.setBudget(preferences.getBudget());
            existingPrefs.setPace(preferences.getPace());
            existingPrefs.setInterests(preferences.getInterests());
            existingPrefs.setAccommodationStyle(preferences.getAccommodationStyle());
            existingPrefs.setTransport(preferences.getTransport());
            existingPrefs.setFoodPreferences(preferences.getFoodPreferences());
            existingPrefs.setSeason(preferences.getSeason());
            return travelPreferencesRepository.save(existingPrefs);
        } else {
            // Create new preferences
            preferences.setUserId(userId);
            return travelPreferencesRepository.save(preferences);
        }
    }

    /**
     * Delete travel preferences for a user
     * @param userId Supabase user ID
     */
    @Transactional
    public void deletePreferences(String userId) {
        travelPreferencesRepository.deleteByUserId(userId);
    }

    /**
     * Check if user has saved preferences
     * @param userId Supabase user ID
     * @return true if preferences exist
     */
    public boolean hasPreferences(String userId) {
        return travelPreferencesRepository.existsByUserId(userId);
    }
}
