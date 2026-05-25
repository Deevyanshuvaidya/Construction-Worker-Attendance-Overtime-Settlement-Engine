package com.company.attendance.worker.controller;

import com.company.attendance.common.dto.PagedResponse;
import com.company.attendance.worker.dto.*;
import com.company.attendance.worker.entity.Designation;
import com.company.attendance.worker.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing construction workers.
 *
 * <p>Exposes endpoints for CRUD operations, status toggling,
 * and paginated search with optional filters.</p>
 */
@RestController
@RequestMapping("/api/v1/workers")
@RequiredArgsConstructor
@Tag(name = "Worker Management", description = "APIs for managing construction workers")
public class WorkerController {

    private final WorkerService workerService;

    /**
     * Creates a new worker.
     *
     * @param request the worker creation request
     * @return the created worker with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create a new worker", description = "Registers a new construction worker in the system")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Worker created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Worker with the same phone number already exists")
    })
    public ResponseEntity<WorkerResponse> createWorker(@Valid @RequestBody WorkerCreateRequest request) {
        WorkerResponse response = workerService.createWorker(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a worker by ID.
     *
     * @param workerId the worker ID
     * @return the worker response with HTTP 200
     */
    @GetMapping("/{workerId}")
    @Operation(summary = "Get worker by ID", description = "Retrieves details of a specific worker")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Worker retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Worker not found")
    })
    public ResponseEntity<WorkerResponse> getWorkerById(
            @Parameter(description = "Worker ID") @PathVariable Long workerId) {
        WorkerResponse response = workerService.getWorkerById(workerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a paginated, filterable list of workers.
     *
     * @param page         zero-based page index (default 0)
     * @param size         page size (default 20)
     * @param sortBy       sort field (default "id")
     * @param sortDir      sort direction (default "asc")
     * @param name         optional name filter
     * @param designation  optional designation filter
     * @param activeStatus optional active-status filter
     * @return a paged response of workers
     */
    @GetMapping
    @Operation(summary = "Get all workers", description = "Returns a paginated list of workers with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workers retrieved successfully")
    })
    public ResponseEntity<PagedResponse<WorkerResponse>> getAllWorkers(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String sortDir,

            @Parameter(description = "Filter by worker name (partial match)")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by designation")
            @RequestParam(required = false) Designation designation,

            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean activeStatus) {

        PagedResponse<WorkerResponse> response = workerService.getAllWorkers(
                page, size, sortBy, sortDir, name, designation, activeStatus);
        return ResponseEntity.ok(response);
    }

    /**
     * Partially updates a worker.
     *
     * @param workerId the worker ID
     * @param request  the update request
     * @return the updated worker response with HTTP 200
     */
    @PutMapping("/{workerId}")
    @Operation(summary = "Update a worker", description = "Partially updates an existing worker's details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Worker updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Worker not found"),
            @ApiResponse(responseCode = "409", description = "Phone number already in use")
    })
    public ResponseEntity<WorkerResponse> updateWorker(
            @Parameter(description = "Worker ID") @PathVariable Long workerId,
            @Valid @RequestBody WorkerUpdateRequest request) {
        WorkerResponse response = workerService.updateWorker(workerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft-deletes a worker.
     *
     * @param workerId the worker ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{workerId}")
    @Operation(summary = "Delete a worker", description = "Soft-deletes a worker by deactivating their status")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Worker deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Worker not found"),
            @ApiResponse(responseCode = "422", description = "Worker is currently clocked in")
    })
    public ResponseEntity<Void> deleteWorker(
            @Parameter(description = "Worker ID") @PathVariable Long workerId) {
        workerService.deleteWorker(workerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the active status of a worker.
     *
     * @param workerId the worker ID
     * @param request  the status update request
     * @return the updated worker response with HTTP 200
     */
    @PatchMapping("/{workerId}/status")
    @Operation(summary = "Update worker status", description = "Activates or deactivates a worker")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Worker status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Worker not found"),
            @ApiResponse(responseCode = "422", description = "Worker is currently clocked in and cannot be deactivated")
    })
    public ResponseEntity<WorkerResponse> updateWorkerStatus(
            @Parameter(description = "Worker ID") @PathVariable Long workerId,
            @Valid @RequestBody WorkerStatusUpdateRequest request) {
        WorkerResponse response = workerService.updateWorkerStatus(workerId, request);
        return ResponseEntity.ok(response);
    }
}
