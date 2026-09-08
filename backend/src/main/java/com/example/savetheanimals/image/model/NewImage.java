package com.example.savetheanimals.image.model;

import java.time.Instant;

/**
 * An image on its way into storage, before the database has assigned it an id.
 *
 * <p>Holds the raw bytes, so it is deliberately short-lived: it exists only between
 * {@link ImageService} validating an upload and {@link ImageRepository} writing it.
 */
public record NewImage(
        Animal animal,
        String contentType,
        String sourceUrl,
        byte[] data,
        Instant createdAt) {

    public long sizeBytes() {
        return data.length;
    }

}
