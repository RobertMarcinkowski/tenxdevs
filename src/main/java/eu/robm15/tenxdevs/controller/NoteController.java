package eu.robm15.tenxdevs.controller;

import eu.robm15.tenxdevs.model.Note;
import eu.robm15.tenxdevs.service.NoteService;
import eu.robm15.tenxdevs.service.SupabaseJwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing travel notes.
 * Provides endpoints for CRUD operations on notes.
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired(required = false) // Optional for localh2 profile
    private SupabaseJwtService jwtService;

    /**
     * Get all notes for the authenticated user.
     *
     * @param request HTTP request containing JWT token
     * @return List of notes belonging to the user
     */
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes(HttpServletRequest request) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Note> notes = noteService.getAllNotesByUserId(userId);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get a specific note by ID.
     * Ensures the note belongs to the authenticated user.
     *
     * @param id The note ID
     * @param request HTTP request containing JWT token
     * @return The requested note if found and owned by the user
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id, HttpServletRequest request) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return noteService.getNoteByIdAndUserId(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new note for the authenticated user.
     *
     * @param noteRequest The note data (title and content)
     * @param request HTTP request containing JWT token
     * @return The created note
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNote(
            @RequestBody NoteRequest noteRequest,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate input
        if (noteRequest.getTitle() == null || noteRequest.getTitle().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Title is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (noteRequest.getContent() == null || noteRequest.getContent().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Content is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            Note note = noteService.createNote(userId, noteRequest.getTitle(), noteRequest.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Note created successfully");
            response.put("note", note);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create note: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update an existing note.
     * Ensures the note belongs to the authenticated user.
     *
     * @param id The note ID
     * @param noteRequest The updated note data
     * @param request HTTP request containing JWT token
     * @return The updated note
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateNote(
            @PathVariable Long id,
            @RequestBody NoteRequest noteRequest,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate input
        if (noteRequest.getTitle() == null || noteRequest.getTitle().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Title is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (noteRequest.getContent() == null || noteRequest.getContent().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Content is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            return noteService.updateNote(id, userId, noteRequest.getTitle(), noteRequest.getContent())
                    .map(note -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "Note updated successfully");
                        response.put("note", note);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "Note not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                    });
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update note: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete a note by ID.
     * Ensures the note belongs to the authenticated user.
     *
     * @param id The note ID
     * @param request HTTP request containing JWT token
     * @return Success or error response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNote(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String userId = extractUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            boolean deleted = noteService.deleteNote(id, userId);

            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Note deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Note not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete note: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Extract user ID from JWT token in the request.
     *
     * @param request HTTP request
     * @return User ID from JWT or mock user ID for localh2 profile
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
     * DTO class for note creation and update requests.
     */
    public static class NoteRequest {
        private String title;
        private String content;

        public NoteRequest() {
        }

        public NoteRequest(String title, String content) {
            this.title = title;
            this.content = content;
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
    }
}
