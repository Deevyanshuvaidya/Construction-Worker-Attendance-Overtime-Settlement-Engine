package com.company.attendance.common.exception;

/**
 * Thrown when a requested resource cannot be found in the data store.
 *
 * <p>Carries the resource name, lookup field, and the value that was
 * searched for, enabling the global exception handler to produce a
 * descriptive 404 response.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructs a new {@code ResourceNotFoundException}.
     *
     * @param resourceName the type of resource (e.g., "Worker", "AttendanceRecord")
     * @param fieldName    the field used for the lookup (e.g., "id")
     * @param fieldValue   the value that was not found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
