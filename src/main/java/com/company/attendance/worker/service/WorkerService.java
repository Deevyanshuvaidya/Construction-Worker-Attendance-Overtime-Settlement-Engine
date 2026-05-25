package com.company.attendance.worker.service;

import com.company.attendance.common.dto.PagedResponse;
import com.company.attendance.worker.dto.*;
import com.company.attendance.worker.entity.Designation;

/**
 * Service interface for worker management operations.
 *
 * <p>Defines the contract for CRUD operations, status updates,
 * and paginated search with filters.</p>
 */
public interface WorkerService {

    /**
     * Creates a new worker.
     *
     * @param request the worker creation request
     * @return the created worker response
     */
    WorkerResponse createWorker(WorkerCreateRequest request);

    /**
     * Partially updates an existing worker.
     *
     * @param workerId the ID of the worker to update
     * @param request  the update request containing non-null fields to apply
     * @return the updated worker response
     */
    WorkerResponse updateWorker(Long workerId, WorkerUpdateRequest request);

    /**
     * Soft-deletes a worker by setting their status to inactive.
     *
     * @param workerId the ID of the worker to delete
     */
    void deleteWorker(Long workerId);

    /**
     * Retrieves a worker by their ID.
     *
     * @param workerId the worker ID
     * @return the worker response
     */
    WorkerResponse getWorkerById(Long workerId);

    /**
     * Returns a paginated, filtered list of workers.
     *
     * @param page         zero-based page index
     * @param size         page size
     * @param sortBy       field to sort by
     * @param sortDir      sort direction ("asc" or "desc")
     * @param name         optional name filter (partial match)
     * @param designation  optional designation filter
     * @param activeStatus optional active-status filter
     * @return a paginated response of workers
     */
    PagedResponse<WorkerResponse> getAllWorkers(int page, int size, String sortBy, String sortDir,
                                                 String name, Designation designation, Boolean activeStatus);

    /**
     * Updates the active status of a worker.
     *
     * @param workerId the worker ID
     * @param request  the status update request
     * @return the updated worker response
     */
    WorkerResponse updateWorkerStatus(Long workerId, WorkerStatusUpdateRequest request);
}
