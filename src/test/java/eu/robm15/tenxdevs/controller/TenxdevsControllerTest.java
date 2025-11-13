package eu.robm15.tenxdevs.controller;

import eu.robm15.tenxdevs.util.JwtTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "supabase.url=http://localhost:54321",
    "supabase.jwt-secret=test-secret-key-must-be-at-least-256-bits-long-for-hs256"
})
public class TenxdevsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${supabase.jwt-secret}")
    private String jwtSecret;

    @Test
    void landingPage() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("landing"));
    }

    @Test
    void apiStatus() throws Exception {
        mockMvc.perform(get("/api/status"))
            .andExpect(status().isOk())
            .andExpect(content().string("Tenxdevs API v 01"));
    }

    @Test
    void tenxdevsDefault() throws Exception {
        mockMvc.perform(get("/tenxdevs"))
            .andExpect(status().isOk())
            .andExpect(content().string("Docker test 11. Hello World!"));
    }

    @Test
    void tenxdevsWithProperty() throws Exception {
        mockMvc.perform(get("/tenxdevs").param("name", "Robert"))
            .andExpect(status().isOk())
            .andExpect(content().string("Docker test 11. Hello Robert!"));
    }

    @Test
    void appPageIsPublic() throws Exception {
        // /app endpoint is now public (auth checked client-side)
        mockMvc.perform(get("/app"))
            .andExpect(status().isOk())
            .andExpect(view().name("app"));
    }

    @Test
    void registerPageIsPublic() throws Exception {
        mockMvc.perform(get("/register"))
            .andExpect(status().isOk())
            .andExpect(view().name("register"));
    }

    @Test
    void authStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void authMeEndpointRequiresAuthentication() throws Exception {
        // Without token - should return 401 or 403
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void authMeEndpointWithValidJwt() throws Exception {
        // Generate a valid test JWT token
        String token = JwtTestUtil.generateTestToken(jwtSecret, "test-user-123", "testuser@example.com");

        // With valid token - should return user info
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.principal").value("testuser@example.com"));
    }

}
