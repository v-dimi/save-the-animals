package com.example.savetheanimals.image.exception;

/** No such image, or nothing stored yet. Surfaces as HTTP 404. */
public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException(String message) {
        super(message);
    }
}
