package com.company.attendance.common.exception;

import com.company.attendance.common.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handler that translates application exceptions into
 * consistent {@link ApiErrorResponse} payloads.
 *
 * <p>Each handler logs the error at an appropriate level and returns an HTTP
 * response with the correct status code and a structured JSON body.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link ResourceNotFoundException} → HTTP 404.
     *
     * @param ex the exception
     * @return a 404 response with error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .error("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles {@link DuplicateResourceException} → HTTP 409.
     *
     * @param ex the exception
     * @return a 409 response with error details
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .error("DUPLICATE_RESOURCE")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles {@link BusinessRuleViolationException} → HTTP 422.
     *
     * @param ex the exception
     * @return a 422 response with the business rule error code
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleViolation(BusinessRuleViolationException ex) {
        log.warn("Business rule violation [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Handles {@link InvalidOperationException} → HTTP 400.
     *
     * @param ex the exception
     * @return a 400 response with the operation error code
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOperation(InvalidOperationException ex) {
        log.warn("Invalid operation [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles Bean Validation failures → HTTP 400 with per-field details.
     *
     * @param ex the exception containing binding result errors
     * @return a 400 response with field-level error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        log.warn("Validation failed: {}", details);
        ApiErrorResponse response = ApiErrorResponse.builder()
                .error("VALIDATION_FAILED")
                .message("Request validation failed")
                .timestamp(Instant.now())
                .details(details)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles database constraint violations → HTTP 409.
     *
     * @param ex the exception
     * @return a 409 response indicating a data integrity violation
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .error("DATA_INTEGRITY_VIOLATION")
                .message("A data integrity constraint was violated")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Catch-all handler for unexpected exceptions → HTTP 500.
     *
     * @param ex the exception
     * @return a 500 response with a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        ApiErrorResponse response = ApiErrorResponse.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please contact support.")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
