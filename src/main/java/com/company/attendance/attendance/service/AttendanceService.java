package com.company.attendance.attendance.service;

import com.company.attendance.attendance.dto.*;
import com.company.attendance.common.dto.PagedResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface defining the core attendance operations:
 * clock-in, clock-out, active worker queries, and attendance log retrieval.
 */
public interface AttendanceService {

    /**
     * Clocks a worker in at a specific site.
     *
     * @param request the clock-in request containing workerId and siteId
     * @return the created attendance log response
     */
    AttendanceLogResponse clockIn(ClockInRequest request);

    /**
     * Clocks a worker out, calculating total hours and overtime.
     *
     * @param request the clock-out request containing workerId
     * @return the updated attendance log response
     */
    AttendanceLogResponse clockOut(ClockOutRequest request);

    /**
     * Returns all workers currently clocked-in (from Redis cache).
     *
     * @return list of active worker session details
     */
    List<ActiveWorkerResponse> getActiveWorkers();

    /**
     * Retrieves paginated attendance logs with optional filters.
     *
     * @param workerId optional worker ID filter
     * @param from     optional start timestamp filter
     * @param to       optional end timestamp filter
     * @param page     page number (zero-based)
     * @param size     page size
     * @param sortBy   field to sort by
     * @param sortDir  sort direction (asc/desc)
     * @return paginated attendance log responses
     */
    PagedResponse<AttendanceLogResponse> getAttendanceLogs(Long workerId, LocalDateTime from, LocalDateTime to,
                                                            int page, int size, String sortBy, String sortDir);
}
