package com.example.savetheanimals.image.model;

import java.time.Instant;

/**
 * Everything about a stored image except the bytes. This is the JSON shape returned by
 * the API, and the shape read back from the database when the caller only needs to know
 * <em>which</em> image is stored — the {@code data} column is not selected, so listing
 * the latest image never pulls a blob off disk.
 */
public record ImageMetadata(
        long id,
        Animal animal,
        String contentType,
        String sourceUrl,
        long sizeBytes,
        Instant createdAt) {
}
