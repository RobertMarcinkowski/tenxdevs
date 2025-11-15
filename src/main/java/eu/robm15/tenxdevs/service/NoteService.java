package eu.robm15.tenxdevs.service;

import eu.robm15.tenxdevs.model.Note;
import eu.robm15.tenxdevs.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing travel notes.
 * Handles business logic for CRUD operations on notes.
 */
@Service
public class NoteService {

    private final NoteRepository noteRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * Create a new note for a user.
     *
     * @param userId The user ID from Supabase
     * @param title The note title
     * @param content The note content
     * @return The created note
     */
    @Transactional
    public Note createNote(String userId, String title, String content) {
        Note note = new Note(userId, title, content);
        return noteRepository.save(note);
    }

    /**
     * Get all notes for a specific user, ordered by creation date (newest first).
     *
     * @param userId The user ID
     * @return List of notes
     */
    public List<Note> getAllNotesByUserId(String userId) {
        return noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get a specific note by ID, ensuring it belongs to the user.
     *
     * @param noteId The note ID
     * @param userId The user ID
     * @return Optional containing the note if found and owned by the user
     */
    public Optional<Note> getNoteByIdAndUserId(Long noteId, String userId) {
        return noteRepository.findByIdAndUserId(noteId, userId);
    }

    /**
     * Update an existing note.
     * Verifies that the note belongs to the user before updating.
     *
     * @param noteId The note ID
     * @param userId The user ID
     * @param title The new title
     * @param content The new content
     * @return Optional containing the updated note if found and owned by the user
     */
    @Transactional
    public Optional<Note> updateNote(Long noteId, String userId, String title, String content) {
        Optional<Note> existingNote = noteRepository.findByIdAndUserId(noteId, userId);

        if (existingNote.isPresent()) {
            Note note = existingNote.get();
            note.setTitle(title);
            note.setContent(content);
            Note updatedNote = noteRepository.save(note);
            return Optional.of(updatedNote);
        }

        return Optional.empty();
    }

    /**
     * Delete a note by ID.
     * Verifies that the note belongs to the user before deleting.
     *
     * @param noteId The note ID
     * @param userId The user ID
     * @return true if the note was deleted, false if not found or not owned by the user
     */
    @Transactional
    public boolean deleteNote(Long noteId, String userId) {
        if (noteRepository.existsByIdAndUserId(noteId, userId)) {
            noteRepository.deleteByIdAndUserId(noteId, userId);
            return true;
        }
        return false;
    }
}
