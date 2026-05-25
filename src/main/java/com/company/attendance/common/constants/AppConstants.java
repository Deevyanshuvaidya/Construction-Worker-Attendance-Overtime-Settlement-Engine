package com.company.attendance.common.constants;

import java.math.BigDecimal;

/**
 * Application-wide constants for the Construction Attendance Engine.
 *
 * <p>All overtime calculation thresholds, Redis key prefixes, and pagination
 * defaults are centralised here to ensure consistency across services and
 * to simplify configuration audits.</p>
 */
public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // ── Overtime Calculation Constants ──────────────────────────────────

    /** Standard shift duration before overtime begins. */
    public static final BigDecimal STANDARD_SHIFT_HOURS = new BigDecimal("8");

    /** Maximum hours eligible for Tier-1 (1.5×) overtime rate. */
    public static final BigDecimal OVERTIME_TIER1_MAX_HOURS = new BigDecimal("2");

    /** Multiplier applied to the base rate for Tier-1 overtime hours. */
    public static final BigDecimal OVERTIME_TIER1_RATE = new BigDecimal("1.5");

    /** Multiplier applied to the base rate for Tier-2 overtime hours. */
    public static final BigDecimal OVERTIME_TIER2_RATE = new BigDecimal("2.0");

    /** Maximum overtime hours a worker may accumulate in a calendar month. */
    public static final BigDecimal MONTHLY_OVERTIME_CAP_HOURS = new BigDecimal("60");

    /** Absolute maximum shift length (safety guard). */
    public static final BigDecimal MAX_SHIFT_HOURS = new BigDecimal("16");

    // ── Redis Constants ────────────────────────────────────────────────

    /** Redis key prefix for tracking actively checked-in workers. */
    public static final String ACTIVE_WORKER_REDIS_PREFIX = "active_worker:";

    /** TTL (in hours) for active-worker Redis entries. */
    public static final long ACTIVE_WORKER_TTL_HOURS = 16;

    // ── Pagination Defaults ────────────────────────────────────────────

    /** Default zero-based page number for paginated queries. */
    public static final String DEFAULT_PAGE_NUMBER = "0";

    /** Default page size for paginated queries. */
    public static final String DEFAULT_PAGE_SIZE = "10";

    /** Default field to sort results by. */
    public static final String DEFAULT_SORT_BY = "id";

    /** Default sort direction ({@code asc} or {@code desc}). */
    public static final String DEFAULT_SORT_DIR = "asc";
}
