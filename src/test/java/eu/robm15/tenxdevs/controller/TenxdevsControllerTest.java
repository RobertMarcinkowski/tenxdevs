package eu.robm15.tenxdevs.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TenxdevsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void tenxdevsDefault() throws Exception {
        mockMvc.perform(get("/tenxdevs")).andExpect(status().isOk()).andExpect(content().string("Docker test 5. Hello World!"));
    }

    @Test
    void tenxdevsWithProperty() throws Exception {
        mockMvc.perform(get("/tenxdevs").param("name", "Robert")).andExpect(status().isOk()).andExpect(content().string("Docker test 5. Hello Robert!"));
    }

}
