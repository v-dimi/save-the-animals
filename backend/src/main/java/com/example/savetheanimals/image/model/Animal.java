package com.example.savetheanimals.image.model;

import java.util.Locale;

import com.example.savetheanimals.image.exception.InvalidImageException;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Animal {
    CAT,
    DOG,
    BEAR;

    @JsonValue
    public String token() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Animal from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidImageException("Field 'animal' is required.");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidImageException(
                    "Unknown animal '" + value + "'. Expected one of: cat, dog, bear.");
        }
    }
}
