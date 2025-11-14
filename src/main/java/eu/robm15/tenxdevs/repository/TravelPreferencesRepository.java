package eu.robm15.tenxdevs.repository;

import eu.robm15.tenxdevs.model.TravelPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelPreferencesRepository extends JpaRepository<TravelPreferences, Long> {

    /**
     * Find travel preferences by user ID (Supabase user ID)
     */
    Optional<TravelPreferences> findByUserId(String userId);

    /**
     * Check if preferences exist for a given user ID
     */
    boolean existsByUserId(String userId);

    /**
     * Delete preferences by user ID
     */
    void deleteByUserId(String userId);
}
