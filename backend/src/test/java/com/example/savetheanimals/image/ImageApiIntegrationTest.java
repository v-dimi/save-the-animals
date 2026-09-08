package com.example.savetheanimals.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The whole stack against a real SQLite file: the round trip a user makes when they click
 * Save and then Load last.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ImageApiIntegrationTest {

    @TempDir
    static Path databaseDirectory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbcClient;

    @DynamicPropertySource
    static void useTemporaryDatabaseFile(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:sqlite:" + databaseDirectory.resolve("save-the-animals-it.db"));
    }

    @BeforeEach
    void startFromAnEmptyTable() {
        jdbcClient.sql("DELETE FROM stored_image").update();
    }

    @Test
    @DisplayName("saves an image and serves the identical bytes back")
    void savesAnImageAndServesTheIdenticalBytesBack() throws Exception {
        byte[] original = pngBytes();

        MvcResult created = mockMvc.perform(multipart("/api/images")
                        .file(new MockMultipartFile("file", "cat.png", "image/png", original))
                        .param("animal", "cat")
                        .param("sourceUrl", "/animals/cat/412/380"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.animal").value("cat"))
                .andExpect(jsonPath("$.sizeBytes").value(original.length))
                .andReturn();

        String contentLocation = created.getResponse().getHeader("Location");
        assertThat(contentLocation).isNotNull();

        mockMvc.perform(get("/api/images/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animal").value("cat"))
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.sourceUrl").value("/animals/cat/412/380"));

        byte[] servedBack = mockMvc.perform(get(contentLocation))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(servedBack).isEqualTo(original);
    }

    @Test
    @DisplayName("the latest image is the most recently saved one")
    void latestImageIsTheMostRecentlySavedOne() throws Exception {
        save("cat", pngBytes());
        save("bear", pngBytes());

        mockMvc.perform(get("/api/images/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animal").value("bear"));
    }

    @Test
    @DisplayName("reports 404 while nothing has been saved")
    void reportsNotFoundWhileNothingHasBeenSaved() throws Exception {
        mockMvc.perform(get("/api/images/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Image not found"));
    }

    @Test
    @DisplayName("refuses a payload that is not an image")
    void refusesNonImagePayload() throws Exception {
        mockMvc.perform(multipart("/api/images")
                        .file(new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes()))
                        .param("animal", "dog"))
                .andExpect(status().isUnsupportedMediaType());

        mockMvc.perform(get("/api/images/latest")).andExpect(status().isNotFound());
    }

    private void save(String animal, byte[] bytes) throws Exception {
        mockMvc.perform(multipart("/api/images")
                        .file(new MockMultipartFile("file", animal + ".png", "image/png", bytes))
                        .param("animal", animal))
                .andExpect(status().isCreated());
    }

    /** A genuinely valid PNG, so the test proves real image bytes survive the round trip. */
    private static byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        image.setRGB(1, 1, 0x00FF7F);
        var out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
