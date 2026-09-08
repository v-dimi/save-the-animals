package com.example.savetheanimals.image.data;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import com.example.savetheanimals.image.model.Animal;
import com.example.savetheanimals.image.model.ImageContent;
import com.example.savetheanimals.image.model.ImageMetadata;
import com.example.savetheanimals.image.model.NewImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the repository against a real SQLite file in a temporary directory — an
 * in-memory substitute would not prove that BLOB round-tripping works on the actual driver.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ImageRepository.class)
class ImageRepositoryTest {

    @TempDir
    static Path databaseDirectory;

    @Autowired
    private ImageRepository repository;

    @Autowired
    private JdbcClient jdbcClient;

    @DynamicPropertySource
    static void useTemporaryDatabaseFile(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:sqlite:" + databaseDirectory.resolve("save-the-animals-test.db"));
    }

    @BeforeEach
    void startFromAnEmptyTable() {
        jdbcClient.sql("DELETE FROM stored_image").update();
    }

    @Test
    @DisplayName("stores image bytes and reads them back unchanged")
    void storesImageBytesAndReadsThemBackUnchanged() {
        byte[] bytes = binaryPayload();

        ImageMetadata saved = repository.save(newImage(Animal.DOG, bytes, Instant.parse("2026-09-08T10:15:30Z")));

        Optional<ImageContent> content = repository.findContentById(saved.id());
        assertThat(content).isPresent();
        assertThat(content.get().data()).isEqualTo(bytes);
        assertThat(content.get().contentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("returns the saved row with a generated id and the supplied metadata")
    void returnsSavedRowWithGeneratedId() {
        byte[] bytes = binaryPayload();
        Instant createdAt = Instant.parse("2026-09-08T10:15:30Z");

        ImageMetadata saved = repository.save(
                new NewImage(Animal.BEAR, "image/png", "/animals/bear/412/380", bytes, createdAt));

        assertThat(saved.id()).isPositive();
        assertThat(saved.animal()).isEqualTo(Animal.BEAR);
        assertThat(saved.contentType()).isEqualTo("image/png");
        assertThat(saved.sourceUrl()).isEqualTo("/animals/bear/412/380");
        assertThat(saved.sizeBytes()).isEqualTo(bytes.length);
        assertThat(saved.createdAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("finds the most recently saved image")
    void findsMostRecentlySavedImage() {
        repository.save(newImage(Animal.CAT, "first".getBytes(), Instant.parse("2026-09-08T10:00:00Z")));
        repository.save(newImage(Animal.DOG, "second".getBytes(), Instant.parse("2026-09-08T11:00:00Z")));
        ImageMetadata third = repository.save(
                newImage(Animal.BEAR, "third".getBytes(), Instant.parse("2026-09-08T12:00:00Z")));

        Optional<ImageMetadata> latest = repository.findLatestMetadata();

        assertThat(latest).contains(third);
    }

    @Test
    @DisplayName("breaks ties on insertion order, not on the timestamp")
    void breaksTiesOnInsertionOrderRatherThanTimestamp() {
        // Two saves inside the same millisecond tie on created_at; only the id can order them.
        Instant sameInstant = Instant.parse("2026-09-08T10:15:30Z");
        repository.save(newImage(Animal.CAT, "earlier".getBytes(), sameInstant));
        ImageMetadata later = repository.save(newImage(Animal.DOG, "later".getBytes(), sameInstant));

        assertThat(repository.findLatestMetadata()).contains(later);
    }

    @Test
    @DisplayName("reports no latest image when nothing is stored")
    void reportsNoLatestImageWhenNothingIsStored() {
        assertThat(repository.findLatestMetadata()).isEmpty();
    }

    @Test
    @DisplayName("reports no content for an unknown id")
    void reportsNoContentForUnknownId() {
        assertThat(repository.findContentById(4711L)).isEmpty();
    }

    @Test
    @DisplayName("stores a null source url without complaint")
    void storesNullSourceUrl() {
        ImageMetadata saved = repository.save(
                new NewImage(Animal.CAT, "image/jpeg", null, binaryPayload(), Instant.now()));

        assertThat(repository.findLatestMetadata()).isPresent();
        assertThat(repository.findLatestMetadata().get().sourceUrl()).isNull();
        assertThat(saved.sourceUrl()).isNull();
    }

    private static NewImage newImage(Animal animal, byte[] data, Instant createdAt) {
        return new NewImage(animal, "image/jpeg", "/animals/" + animal.name().toLowerCase() + "/412/380",
                data, createdAt);
    }

    /** Includes null and high bytes, which is what would break a text-column mistake. */
    private static byte[] binaryPayload() {
        return new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x00, 0x1A, 0x0A, (byte) 0xFF, 0x00, 0x42};
    }
}
