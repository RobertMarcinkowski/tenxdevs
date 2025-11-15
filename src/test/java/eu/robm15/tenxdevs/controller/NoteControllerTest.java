package eu.robm15.tenxdevs.controller;

import eu.robm15.tenxdevs.model.Note;
import eu.robm15.tenxdevs.repository.NoteRepository;
import eu.robm15.tenxdevs.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "supabase.url=http://localhost:54321",
    "supabase.jwt-secret=test-secret-key-must-be-at-least-256-bits-long-for-hs256",
    "spring.ai.openai.api-key=test-api-key",
    "spring.ai.openai.chat.options.model=gpt-4",
    "spring.ai.openai.chat.options.temperature=0.7"
})
public class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoteRepository noteRepository;

    @Value("${supabase.jwt-secret}")
    private String jwtSecret;

    private String testUserId = "test-user-123";
    private String testUserEmail = "testuser@example.com";
    private String testToken;

    @BeforeEach
    void setUp() {
        // Generate a valid test JWT token
        testToken = JwtTestUtil.generateTestToken(jwtSecret, testUserId, testUserEmail);

        // Clean up test data before each test
        noteRepository.deleteAll();
    }

    @Test
    void getAllNotesRequiresAuthentication() throws Exception {
        // Without token - should return 401 or 403
        mockMvc.perform(get("/api/notes"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllNotesWithValidJwtReturnsEmptyList() throws Exception {
        // With valid token but no notes - should return empty array
        mockMvc.perform(get("/api/notes")
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllNotesWithValidJwtReturnsUserNotes() throws Exception {
        // Create test notes
        Note note1 = new Note(testUserId, "Trip to Paris", "Visit Eiffel Tower and Louvre");
        Note note2 = new Note(testUserId, "Weekend in Rome", "Colosseum, Vatican, Trevi Fountain");
        noteRepository.save(note1);
        noteRepository.save(note2);

        // With valid token - should return user's notes
        mockMvc.perform(get("/api/notes")
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title", is("Weekend in Rome"))) // Newest first
            .andExpect(jsonPath("$[1].title", is("Trip to Paris")));
    }

    @Test
    void getAllNotesOnlyReturnsCurrentUserNotes() throws Exception {
        // Create notes for test user
        Note userNote = new Note(testUserId, "My Trip", "My content");
        noteRepository.save(userNote);

        // Create notes for another user
        Note otherUserNote = new Note("other-user-456", "Other Trip", "Other content");
        noteRepository.save(otherUserNote);

        // Should only return the current user's notes
        mockMvc.perform(get("/api/notes")
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("My Trip")));
    }

    @Test
    void createNoteRequiresAuthentication() throws Exception {
        String noteJson = "{\"title\":\"Test Note\",\"content\":\"Test Content\"}";

        // Without token - should return 401 or 403
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(noteJson))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void createNoteWithValidData() throws Exception {
        String noteJson = "{\"title\":\"Trip to Barcelona\",\"content\":\"Visit Sagrada Familia and Park Guell\"}";

        mockMvc.perform(post("/api/notes")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(noteJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.message", containsString("created")))
            .andExpect(jsonPath("$.note.title", is("Trip to Barcelona")))
            .andExpect(jsonPath("$.note.content", is("Visit Sagrada Familia and Park Guell")))
            .andExpect(jsonPath("$.note.userId", is(testUserId)));
    }

    @Test
    void createNoteWithMissingTitle() throws Exception {
        String noteJson = "{\"content\":\"Test Content\"}";

        mockMvc.perform(post("/api/notes")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(noteJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", containsString("Title is required")));
    }

    @Test
    void createNoteWithMissingContent() throws Exception {
        String noteJson = "{\"title\":\"Test Title\"}";

        mockMvc.perform(post("/api/notes")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(noteJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", containsString("Content is required")));
    }

    @Test
    void createNoteWithEmptyTitle() throws Exception {
        String noteJson = "{\"title\":\"   \",\"content\":\"Test Content\"}";

        mockMvc.perform(post("/api/notes")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(noteJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", containsString("Title is required")));
    }

    @Test
    void getNoteByIdRequiresAuthentication() throws Exception {
        Note note = new Note(testUserId, "Test Note", "Test Content");
        note = noteRepository.save(note);

        // Without token - should return 401 or 403
        mockMvc.perform(get("/api/notes/" + note.getId()))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void getNoteByIdWithValidJwt() throws Exception {
        Note note = new Note(testUserId, "Trip to London", "Big Ben, Tower Bridge");
        note = noteRepository.save(note);

        mockMvc.perform(get("/api/notes/" + note.getId())
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title", is("Trip to London")))
            .andExpect(jsonPath("$.content", is("Big Ben, Tower Bridge")))
            .andExpect(jsonPath("$.userId", is(testUserId)));
    }

    @Test
    void getNoteByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/notes/999999")
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void getNoteByIdCannotAccessOtherUsersNotes() throws Exception {
        // Create a note for another user
        Note otherUserNote = new Note("other-user-456", "Other User Note", "Secret Content");
        otherUserNote = noteRepository.save(otherUserNote);

        // Try to access it with current user's token
        mockMvc.perform(get("/api/notes/" + otherUserNote.getId())
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isNotFound()); // Should not be able to access
    }

    @Test
    void updateNoteRequiresAuthentication() throws Exception {
        Note note = new Note(testUserId, "Test Note", "Test Content");
        note = noteRepository.save(note);

        String updateJson = "{\"title\":\"Updated Title\",\"content\":\"Updated Content\"}";

        // Without token - should return 401 or 403
        mockMvc.perform(put("/api/notes/" + note.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void updateNoteWithValidData() throws Exception {
        Note note = new Note(testUserId, "Original Title", "Original Content");
        note = noteRepository.save(note);

        String updateJson = "{\"title\":\"Updated Title\",\"content\":\"Updated Content\"}";

        mockMvc.perform(put("/api/notes/" + note.getId())
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.message", containsString("updated")))
            .andExpect(jsonPath("$.note.title", is("Updated Title")))
            .andExpect(jsonPath("$.note.content", is("Updated Content")));
    }

    @Test
    void updateNoteNotFound() throws Exception {
        String updateJson = "{\"title\":\"Updated Title\",\"content\":\"Updated Content\"}";

        mockMvc.perform(put("/api/notes/999999")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void updateNoteCannotUpdateOtherUsersNotes() throws Exception {
        // Create a note for another user
        Note otherUserNote = new Note("other-user-456", "Other User Note", "Secret Content");
        otherUserNote = noteRepository.save(otherUserNote);

        String updateJson = "{\"title\":\"Hacked Title\",\"content\":\"Hacked Content\"}";

        // Try to update it with current user's token
        mockMvc.perform(put("/api/notes/" + otherUserNote.getId())
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void deleteNoteRequiresAuthentication() throws Exception {
        Note note = new Note(testUserId, "Test Note", "Test Content");
        note = noteRepository.save(note);

        // Without token - should return 401 or 403
        mockMvc.perform(delete("/api/notes/" + note.getId()))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void deleteNoteWithValidJwt() throws Exception {
        Note note = new Note(testUserId, "Note to Delete", "This will be deleted");
        note = noteRepository.save(note);

        mockMvc.perform(delete("/api/notes/" + note.getId())
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.message", containsString("deleted")));

        // Verify the note is actually deleted
        mockMvc.perform(get("/api/notes/" + note.getId())
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteNoteNotFound() throws Exception {
        mockMvc.perform(delete("/api/notes/999999")
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void deleteNoteCannotDeleteOtherUsersNotes() throws Exception {
        // Create a note for another user
        Note otherUserNote = new Note("other-user-456", "Other User Note", "Secret Content");
        otherUserNote = noteRepository.save(otherUserNote);

        // Try to delete it with current user's token
        mockMvc.perform(delete("/api/notes/" + otherUserNote.getId())
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", containsString("not found")));

        // Verify the note still exists
        Note stillExists = noteRepository.findById(otherUserNote.getId()).orElse(null);
        assert stillExists != null;
    }
}
