package com.example.savetheanimals.web;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The endpoint behind the frontend's "Check backend health" button. It has to report on the
 * database too — a backend that cannot reach SQLite is not healthy for this application.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HealthEndpointTest {

    @TempDir
    static Path databaseDirectory;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void useTemporaryDatabaseFile(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:sqlite:" + databaseDirectory.resolve("save-the-animals-health.db"));
    }

    @Test
    @DisplayName("reports UP, including the database component")
    void reportsUpIncludingTheDatabaseComponent() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"));
    }
}
