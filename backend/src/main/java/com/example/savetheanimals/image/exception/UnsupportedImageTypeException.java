package com.example.savetheanimals.image.exception;

/** The upload was well-formed but is not an image. Surfaces as HTTP 415. */
public class UnsupportedImageTypeException extends RuntimeException {

    public UnsupportedImageTypeException(String message) {
        super(message);
    }
}
