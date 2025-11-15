package eu.robm15.tenxdevs.repository;

import eu.robm15.tenxdevs.model.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    /**
     * Find all trip plans for a specific user
     */
    List<TripPlan> findByUserId(String userId);

    /**
     * Find all trip plans for a specific note
     */
    List<TripPlan> findByNoteId(Long noteId);

    /**
     * Find all trip plans for a specific user and note
     */
    List<TripPlan> findByUserIdAndNoteId(String userId, Long noteId);

    /**
     * Count trip plans created by a user after a certain date
     * (useful for tracking AI usage limits)
     */
    long countByUserIdAndCreatedAtAfter(String userId, LocalDateTime date);
}
