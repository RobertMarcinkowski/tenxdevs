package eu.robm15.tenxdevs.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class TenxdevsControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
            .andExpect(content().string("Docker test 9. Hello World!"));
    }

    @Test
    void tenxdevsWithProperty() throws Exception {
        mockMvc.perform(get("/tenxdevs").param("name", "Robert"))
            .andExpect(status().isOk())
            .andExpect(content().string("Docker test 9. Hello Robert!"));
    }

    @Test
    @WithMockUser
    void appPageRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/app"))
            .andExpect(status().isOk())
            .andExpect(view().name("app"));
    }

}
