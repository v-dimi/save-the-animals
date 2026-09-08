package com.example.savetheanimals.image.model;

/** The bytes of a stored image, plus just enough to serve them over HTTP. */
public record ImageContent(byte[] data, String contentType) {
}
