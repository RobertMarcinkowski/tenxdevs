package eu.robm15.tenxdevs.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a travel note created by a user.
 * Notes are simple text-based ideas for trips that can later be used to generate travel plans.
 */
@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    /**
     * User ID from Supabase authentication (UUID string).
     * Links the note to the user who created it.
     */
    @Column(nullable = false)
    private String userId;

    /**
     * Title of the travel note.
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Content/body of the travel note.
     * Can be free-form, unstructured text describing trip ideas.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Timestamp when the note was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the note was last updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Automatically set timestamps before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Automatically update the updatedAt timestamp before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Note() {
    }

    public Note(String userId, String title, String content) {
        this.userId = userId;
        this.title = title;
        this.content = content;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
