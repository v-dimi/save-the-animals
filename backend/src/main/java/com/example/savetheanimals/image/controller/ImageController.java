package com.example.savetheanimals.image.controller;

import java.net.URI;

import com.example.savetheanimals.image.model.ImageContent;
import com.example.savetheanimals.image.model.ImageMetadata;
import com.example.savetheanimals.image.service.ImageService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * The image API.
 *
 * <p>Metadata and bytes are separate resources on purpose: the frontend fetches the small
 * JSON document to learn <em>what</em> is stored, then points an {@code <img>} tag at the
 * content URL. That keeps the JSON small and lets the browser cache the picture normally.
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService service;

    ImageController(ImageService service) {
        this.service = service;
    }

    /** Stores an image the browser fetched from one of the animal services. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ImageMetadata> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("animal") String animal,
            @RequestParam(name = "sourceUrl", required = false) String sourceUrl) {

        ImageMetadata stored = service.store(animal, sourceUrl, file);
        return ResponseEntity.created(contentLocation(stored.id())).body(stored);
    }

    /** The most recently stored image, without its bytes. */
    @GetMapping("/latest")
    ResponseEntity<ImageMetadata> latest() {
        return ResponseEntity.ok()
                .body(service.latest());
    }

    /** The bytes of one stored image. */
    @GetMapping("/{id}/content")
    ResponseEntity<byte[]> content(@PathVariable long id) {
        ImageContent image = service.content(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .contentLength(image.data().length)
                .body(image.data());
    }

    private static URI contentLocation(long id) {
        return URI.create("/api/images/" + id + "/content");
    }
}
