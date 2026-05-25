package com.company.attendance.attendance.controller;

import com.company.attendance.attendance.dto.*;
import com.company.attendance.attendance.service.AttendanceService;
import com.company.attendance.common.constants.AppConstants;
import com.company.attendance.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for attendance management operations including
 * clock-in, clock-out, active worker queries, and attendance log retrieval.
 */
@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance Management", description = "APIs for managing worker attendance clock-in/clock-out and attendance logs")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Clocks a worker in at a specific construction site.
     *
     * @param request the clock-in request body
     * @return the created attendance log
     */
    @PostMapping("/clock-in")
    @Operation(summary = "Clock in a worker", description = "Records a worker's clock-in at a specified construction site")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Clock-in recorded successfully"),
            @ApiResponse(responseCode = "404", description = "Worker or Site not found"),
            @ApiResponse(responseCode = "409", description = "Worker is already clocked in or inactive")
    })
    public ResponseEntity<AttendanceLogResponse> clockIn(@Valid @RequestBody ClockInRequest request) {
        AttendanceLogResponse response = attendanceService.clockIn(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Clocks a worker out, triggering hours and overtime calculations.
     *
     * @param request the clock-out request body
     * @return the updated attendance log with calculated hours
     */
    @PostMapping("/clock-out")
    @Operation(summary = "Clock out a worker", description = "Records a worker's clock-out and calculates total hours and overtime")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clock-out recorded successfully"),
            @ApiResponse(responseCode = "409", description = "Worker is not currently clocked in")
    })
    public ResponseEntity<AttendanceLogResponse> clockOut(@Valid @RequestBody ClockOutRequest request) {
        AttendanceLogResponse response = attendanceService.clockOut(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns all workers that are currently clocked-in (fetched from Redis cache).
     *
     * @return list of active worker sessions
     */
    @GetMapping("/active")
    @Operation(summary = "Get active workers", description = "Retrieves all workers currently clocked in from Redis cache")
    @ApiResponse(responseCode = "200", description = "Active workers retrieved successfully")
    public ResponseEntity<List<ActiveWorkerResponse>> getActiveWorkers() {
        List<ActiveWorkerResponse> activeWorkers = attendanceService.getActiveWorkers();
        return ResponseEntity.ok(activeWorkers);
    }

    /**
     * Retrieves paginated attendance logs with optional filters for worker, date range, and sorting.
     *
     * @param workerId optional worker ID filter
     * @param from     optional start timestamp (ISO date-time)
     * @param to       optional end timestamp (ISO date-time)
     * @param page     page number (zero-based, default 0)
     * @param size     page size (default 20)
     * @param sortBy   sort field (default createdAt)
     * @param sortDir  sort direction: asc or desc (default desc)
     * @return paginated attendance log responses
     */
    @GetMapping("/log")
    @Operation(summary = "Get attendance logs", description = "Retrieves paginated attendance logs with optional filtering by worker and time range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance logs retrieved successfully")
    })
    public ResponseEntity<PagedResponse<AttendanceLogResponse>> getAttendanceLogs(
            @Parameter(description = "Filter by worker ID")
            @RequestParam(required = false) Long workerId,

            @Parameter(description = "Filter from timestamp (ISO date-time, e.g. 2026-01-01T00:00:00)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "Filter to timestamp (ISO date-time, e.g. 2026-01-31T23:59:59)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,

            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,

            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {

        PagedResponse<AttendanceLogResponse> response =
                attendanceService.getAttendanceLogs(workerId, from, to, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
}
