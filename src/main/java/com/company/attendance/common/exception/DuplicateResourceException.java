package com.company.attendance.common.exception;

/**
 * Thrown when an attempt is made to create a resource that already exists.
 *
 * <p>Typically maps to an HTTP 409 Conflict response.</p>
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructs a new {@code DuplicateResourceException}.
     *
     * @param message a descriptive message identifying the duplicate resource
     */
    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code DuplicateResourceException} with resource details.
     *
     * @param resourceName name of the resource type
     * @param fieldName    name of the unique field causing conflict
     * @param fieldValue   value of the unique field causing conflict
     */
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
