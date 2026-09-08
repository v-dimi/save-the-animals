package com.example.savetheanimals.image.controller;

import java.time.Instant;

import com.example.savetheanimals.image.exception.ImageNotFoundException;
import com.example.savetheanimals.image.exception.InvalidImageException;
import com.example.savetheanimals.image.exception.UnsupportedImageTypeException;
import com.example.savetheanimals.image.model.Animal;
import com.example.savetheanimals.image.model.ImageContent;
import com.example.savetheanimals.image.model.ImageMetadata;
import com.example.savetheanimals.image.service.ImageService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-08T10:15:30Z");
    private static final byte[] BYTES = {(byte) 0x89, 'P', 'N', 'G', 0x00, 0x42};

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageService service;

    @Test
    @DisplayName("POST stores the image and answers 201 pointing at its content")
    void postStoresImage() throws Exception {
        when(service.store(eq("dog"), eq("/animals/dog/412/380"), any()))
                .thenReturn(metadata(7L, Animal.DOG));

        mockMvc.perform(multipart("/api/images")
                        .file(new MockMultipartFile("file", "picture.jpg", "image/jpeg", BYTES))
                        .param("animal", "dog")
                        .param("sourceUrl", "/animals/dog/412/380"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/images/7/content"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.animal").value("dog"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.sizeBytes").value(BYTES.length))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("POST without an animal is a 400 problem document")
    void postWithoutAnimalIsBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/images")
                        .file(new MockMultipartFile("file", "picture.jpg", "image/jpeg", BYTES)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("an unknown animal is reported as 400 with an explanation")
    void unknownAnimalIsBadRequest() throws Exception {
        when(service.store(any(), any(), any()))
                .thenThrow(new InvalidImageException("Unknown animal 'axolotl'."));

        mockMvc.perform(multipart("/api/images")
                        .file(new MockMultipartFile("file", "picture.jpg", "image/jpeg", BYTES))
                        .param("animal", "axolotl"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid image upload"))
                .andExpect(jsonPath("$.detail").value("Unknown animal 'axolotl'."));
    }

    @Test
    @DisplayName("a non-image payload is reported as 415")
    void nonImagePayloadIsUnsupportedMediaType() throws Exception {
        when(service.store(any(), any(), any()))
                .thenThrow(new UnsupportedImageTypeException("Content type 'application/json' is not an image."));

        mockMvc.perform(multipart("/api/images")
                        .file(new MockMultipartFile("file", "payload.json", "application/json", BYTES))
                        .param("animal", "cat"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.title").value("Unsupported image type"));
    }

    @Test
    @DisplayName("GET /latest returns the stored metadata")
    void getLatestReturnsMetadata() throws Exception {
        when(service.latest()).thenReturn(metadata(3L, Animal.BEAR));

        mockMvc.perform(get("/api/images/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.animal").value("bear"))
                .andExpect(jsonPath("$.createdAt").value("2026-09-08T10:15:30Z"));
    }

    @Test
    @DisplayName("GET /latest is a 404 problem document when nothing is stored")
    void getLatestIsNotFoundWhenEmpty() throws Exception {
        when(service.latest()).thenThrow(new ImageNotFoundException("No image has been stored yet."));

        mockMvc.perform(get("/api/images/latest"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Image not found"));
    }

    @Test
    @DisplayName("GET content returns the raw bytes with their own content type")
    void getContentReturnsRawBytes() throws Exception {
        when(service.content(3L)).thenReturn(new ImageContent(BYTES, "image/png"));

        mockMvc.perform(get("/api/images/3/content"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(BYTES));
    }

    @Test
    @DisplayName("GET content for an unknown id is a 404")
    void getContentForUnknownIdIsNotFound() throws Exception {
        when(service.content(anyLong())).thenThrow(new ImageNotFoundException("No image with id 4711."));

        mockMvc.perform(get("/api/images/4711/content"))
                .andExpect(status().isNotFound());
    }

    private static ImageMetadata metadata(long id, Animal animal) {
        return new ImageMetadata(id, animal, "image/jpeg",
                "/animals/" + animal.token() + "/412/380", BYTES.length, CREATED_AT);
    }
}
