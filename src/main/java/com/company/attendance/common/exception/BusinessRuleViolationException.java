package com.company.attendance.common.exception;

/**
 * Thrown when a domain-level business rule is violated.
 *
 * <p>Carries a machine-readable {@code errorCode} so that API consumers can
 * programmatically identify the specific rule that was broken (e.g.,
 * {@code OVERTIME_CAP_EXCEEDED}).</p>
 */
public class BusinessRuleViolationException extends RuntimeException {

    private final String errorCode;

    /**
     * Constructs a new {@code BusinessRuleViolationException}.
     *
     * @param errorCode a machine-readable error code
     * @param message   a human-readable description of the violation
     */
    public BusinessRuleViolationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the machine-readable error code for this violation.
     *
     * @return the error code string
     */
    public String getErrorCode() {
        return errorCode;
    }
}
