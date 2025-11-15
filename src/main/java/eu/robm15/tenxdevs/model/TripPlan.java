package eu.robm15.tenxdevs.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a generated trip plan.
 * Each plan is associated with a user and a note, and can be rated.
 */
@Entity
@Table(name = "trip_plans")
public class TripPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    /**
     * Supabase user ID - links plan to authenticated user
     */
    @Column(nullable = false)
    private String userId;

    /**
     * ID of the note that was used to generate this plan
     */
    @Column(nullable = false)
    private Long noteId;

    /**
     * The generated plan content (formatted text with activities per day)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String planContent;

    /**
     * User rating for this plan (1-5 scale, nullable if not rated yet)
     */
    @Column
    private Integer rating;

    /**
     * Timestamp when the plan was generated
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors

    public TripPlan() {
    }

    public TripPlan(String userId, Long noteId, String planContent) {
        this.userId = userId;
        this.noteId = noteId;
        this.planContent = planContent;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public String getPlanContent() {
        return planContent;
    }

    public void setPlanContent(String planContent) {
        this.planContent = planContent;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
