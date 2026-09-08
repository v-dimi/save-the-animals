package com.example.savetheanimals.web;

import com.example.savetheanimals.image.exception.ImageNotFoundException;
import com.example.savetheanimals.image.exception.InvalidImageException;
import com.example.savetheanimals.image.exception.UnsupportedImageTypeException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Turns the application's exceptions into RFC 9457 problem documents, so every failure the
 * frontend can provoke arrives in one shape it knows how to render.
 */
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(InvalidImageException.class)
    ResponseEntity<ProblemDetail> handleInvalidImage(InvalidImageException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid image upload", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedImageTypeException.class)
    ResponseEntity<ProblemDetail> handleUnsupportedType(UnsupportedImageTypeException ex) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported image type", ex.getMessage());
    }

    @ExceptionHandler(ImageNotFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(ImageNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Image not found", ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ProblemDetail> handleTooLarge(MaxUploadSizeExceededException ex) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "Image too large",
                "The uploaded image exceeds the maximum accepted size.");
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(title);
        return ResponseEntity.of(body).build();
    }
}
