package com.example.savetheanimals.image.exception;

/** The client sent something that cannot be stored. Surfaces as HTTP 400. */
public class InvalidImageException extends RuntimeException {

    public InvalidImageException(String message) {
        super(message);
    }
}
