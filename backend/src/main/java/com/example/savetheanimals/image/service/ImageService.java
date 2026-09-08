package com.example.savetheanimals.image.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.Locale;

import com.example.savetheanimals.image.data.ImageRepository;
import com.example.savetheanimals.image.exception.ImageNotFoundException;
import com.example.savetheanimals.image.exception.InvalidImageException;
import com.example.savetheanimals.image.exception.UnsupportedImageTypeException;
import com.example.savetheanimals.image.model.Animal;
import com.example.savetheanimals.image.model.ImageContent;
import com.example.savetheanimals.image.model.ImageMetadata;
import com.example.savetheanimals.image.model.NewImage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class ImageService {

    private static final String IMAGE_TYPE_PREFIX = "image/";

    private final ImageRepository repository;
    private final Clock clock;

    ImageService(ImageRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public ImageMetadata store(String animal, String sourceUrl, MultipartFile file) {
        Animal parsedAnimal = Animal.from(animal);

        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Field 'file' is required and must not be empty.");
        }

        String contentType = normaliseContentType(file.getContentType());
        if (!contentType.startsWith(IMAGE_TYPE_PREFIX)) {
            throw new UnsupportedImageTypeException(
                    "Content type '" + contentType + "' is not an image. Expected image/*.");
        }

        return repository.save(new NewImage(parsedAnimal, contentType, sourceUrl, readBytes(file),
                clock.instant()));
    }

    public ImageMetadata latest() {
        return repository.findLatestMetadata()
                .orElseThrow(() -> new ImageNotFoundException("No image has been stored yet."));
    }

    public ImageContent content(long id) {
        return repository.findContentById(id)
                .orElseThrow(() -> new ImageNotFoundException("No image with id " + id + "."));
    }

    /** Lower-cases and drops any parameters, so "IMAGE/JPEG; charset=binary" becomes "image/jpeg". */
    private static String normaliseContentType(String rawContentType) {
        if (rawContentType == null || rawContentType.isBlank()) {
            return "application/octet-stream";
        }
        int parameterStart = rawContentType.indexOf(';');
        String withoutParameters =
                parameterStart < 0 ? rawContentType : rawContentType.substring(0, parameterStart);
        return withoutParameters.trim().toLowerCase(Locale.ROOT);
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            // Reading an already-buffered upload failing is a server problem, not a client one.
            throw new UncheckedIOException("Could not read the uploaded image.", ex);
        }
    }
}
