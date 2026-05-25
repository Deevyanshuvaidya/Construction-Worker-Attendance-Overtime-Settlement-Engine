package com.company.attendance.overtime.service;

import com.company.attendance.overtime.dto.OvertimeSummaryDTO;

/**
 * Service interface defining overtime operations:
 * retrieving monthly summaries and settling monthly payouts.
 */
public interface OvertimeService {

    /**
     * Retrieves the overtime summary for a worker for a specific year and month.
     *
     * @param workerId the worker's ID
     * @param year the year (e.g. 2026)
     * @param month the month (1-12)
     * @return the overtime summary containing aggregates and daily breakdowns
     */
    OvertimeSummaryDTO getOvertimeSummary(Long workerId, int year, int month);

    /**
     * Settles all pending overtime entries for a worker for a specific past month.
     * Overtime for the current month cannot be settled.
     *
     * @param workerId the worker's ID
     * @param year the year of the month to settle
     * @param month the month to settle (1-12)
     */
    void settleOvertime(Long workerId, int year, int month);
}
