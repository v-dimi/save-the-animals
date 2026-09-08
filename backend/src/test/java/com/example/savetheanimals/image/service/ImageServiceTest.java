package com.example.savetheanimals.image.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import com.example.savetheanimals.image.data.ImageRepository;
import com.example.savetheanimals.image.exception.ImageNotFoundException;
import com.example.savetheanimals.image.exception.InvalidImageException;
import com.example.savetheanimals.image.exception.UnsupportedImageTypeException;
import com.example.savetheanimals.image.model.Animal;
import com.example.savetheanimals.image.model.ImageMetadata;
import com.example.savetheanimals.image.model.NewImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-08T10:15:30Z");
    private static final byte[] BYTES = {(byte) 0x89, 'P', 'N', 'G', 0x00};

    private ImageRepository repository;
    private ImageService service;

    @BeforeEach
    void setUp() {
        repository = mock(ImageRepository.class);
        service = new ImageService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("stores a valid upload and stamps it with the current time")
    void storesValidUpload() {
        when(repository.save(any())).thenAnswer(invocation -> metadataFor(invocation.getArgument(0)));

        service.store("dog", "/animals/dog/412/380", file("image/jpeg", BYTES));

        ArgumentCaptor<NewImage> captor = ArgumentCaptor.forClass(NewImage.class);
        verify(repository).save(captor.capture());
        NewImage saved = captor.getValue();
        assertThat(saved.animal()).isEqualTo(Animal.DOG);
        assertThat(saved.contentType()).isEqualTo("image/jpeg");
        assertThat(saved.sourceUrl()).isEqualTo("/animals/dog/412/380");
        assertThat(saved.data()).isEqualTo(BYTES);
        assertThat(saved.createdAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("accepts the animal name in any case")
    void acceptsAnimalNameInAnyCase() {
        when(repository.save(any())).thenAnswer(invocation -> metadataFor(invocation.getArgument(0)));

        service.store("BeAr", null, file("image/png", BYTES));

        ArgumentCaptor<NewImage> captor = ArgumentCaptor.forClass(NewImage.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().animal()).isEqualTo(Animal.BEAR);
    }

    @Test
    @DisplayName("strips parameters and casing from the content type")
    void normalisesContentType() {
        when(repository.save(any())).thenAnswer(invocation -> metadataFor(invocation.getArgument(0)));

        service.store("cat", null, file("IMAGE/JPEG; charset=binary", BYTES));

        ArgumentCaptor<NewImage> captor = ArgumentCaptor.forClass(NewImage.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().contentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("rejects an unknown animal")
    void rejectsUnknownAnimal() {
        assertThatThrownBy(() -> service.store("axolotl", null, file("image/jpeg", BYTES)))
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("axolotl");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("rejects a missing animal")
    void rejectsMissingAnimal() {
        assertThatThrownBy(() -> service.store("  ", null, file("image/jpeg", BYTES)))
                .isInstanceOf(InvalidImageException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("rejects an empty file")
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> service.store("cat", null, file("image/jpeg", new byte[0])))
                .isInstanceOf(InvalidImageException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("rejects a payload that is not an image")
    void rejectsNonImagePayload() {
        assertThatThrownBy(() -> service.store("cat", null, file("application/json", BYTES)))
                .isInstanceOf(UnsupportedImageTypeException.class)
                .hasMessageContaining("application/json");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("rejects a payload with no declared content type")
    void rejectsMissingContentType() {
        assertThatThrownBy(() -> service.store("cat", null, file(null, BYTES)))
                .isInstanceOf(UnsupportedImageTypeException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("reports that nothing is stored yet")
    void reportsNothingStoredYet() {
        when(repository.findLatestMetadata()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.latest()).isInstanceOf(ImageNotFoundException.class);
    }

    @Test
    @DisplayName("reports an unknown image id")
    void reportsUnknownImageId() {
        when(repository.findContentById(4711L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.content(4711L))
                .isInstanceOf(ImageNotFoundException.class)
                .hasMessageContaining("4711");
    }

    private static MultipartFile file(String contentType, byte[] content) {
        return new MockMultipartFile("file", "picture.jpg", contentType, content);
    }

    private static ImageMetadata metadataFor(NewImage image) {
        return new ImageMetadata(1L, image.animal(), image.contentType(), image.sourceUrl(),
                image.sizeBytes(), image.createdAt());
    }
}
