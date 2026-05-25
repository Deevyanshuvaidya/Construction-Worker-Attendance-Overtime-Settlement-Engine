package com.company.attendance.common.exception;

/**
 * Thrown when a requested operation is invalid in the current context.
 *
 * <p>Examples include attempting to check-out a worker who has not checked in,
 * or trying to re-open an already settled overtime period. Carries a
 * machine-readable {@code errorCode} for programmatic handling.</p>
 */
public class InvalidOperationException extends RuntimeException {

    private final String errorCode;

    /**
     * Constructs a new {@code InvalidOperationException}.
     *
     * @param errorCode a machine-readable error code
     * @param message   a human-readable description of why the operation is invalid
     */
    public InvalidOperationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the machine-readable error code for this invalid operation.
     *
     * @return the error code string
     */
    public String getErrorCode() {
        return errorCode;
    }
}
