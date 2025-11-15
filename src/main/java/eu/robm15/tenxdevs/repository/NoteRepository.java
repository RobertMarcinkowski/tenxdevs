package eu.robm15.tenxdevs.repository;

import eu.robm15.tenxdevs.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Note entity.
 * Provides CRUD operations and custom query methods for notes.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * Find all notes belonging to a specific user, ordered by creation date (newest first).
     *
     * @param userId The Supabase user ID
     * @return List of notes for the user
     */
    List<Note> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find a specific note by ID and user ID.
     * This ensures users can only access their own notes.
     *
     * @param id The note ID
     * @param userId The user ID
     * @return Optional containing the note if found and owned by the user
     */
    Optional<Note> findByIdAndUserId(Long id, String userId);

    /**
     * Delete a note by ID and user ID.
     * This ensures users can only delete their own notes.
     *
     * @param id The note ID
     * @param userId The user ID
     */
    void deleteByIdAndUserId(Long id, String userId);

    /**
     * Check if a note exists for a specific user.
     *
     * @param id The note ID
     * @param userId The user ID
     * @return true if the note exists and belongs to the user
     */
    boolean existsByIdAndUserId(Long id, String userId);
}
