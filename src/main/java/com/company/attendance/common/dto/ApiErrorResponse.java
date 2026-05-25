package com.company.attendance.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response DTO returned by all API error handlers.
 *
 * <p>Provides a consistent JSON error envelope containing an error code,
 * human-readable message, timestamp, and optional field-level validation
 * details.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /** Machine-readable error code (e.g., {@code RESOURCE_NOT_FOUND}). */
    private String error;

    /** Human-readable error description. */
    private String message;

    /** UTC timestamp of when the error occurred. */
    private Instant timestamp;

    /** Optional list of field-level validation error messages. */
    private List<String> details;
}
