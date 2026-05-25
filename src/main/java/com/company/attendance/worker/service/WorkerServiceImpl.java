package com.company.attendance.worker.service;

import com.company.attendance.common.dto.PagedResponse;
import com.company.attendance.common.exception.BusinessRuleViolationException;
import com.company.attendance.common.exception.DuplicateResourceException;
import com.company.attendance.common.exception.ResourceNotFoundException;
import com.company.attendance.worker.dto.*;
import com.company.attendance.worker.entity.Designation;
import com.company.attendance.worker.entity.Worker;
import com.company.attendance.worker.mapper.WorkerMapper;
import com.company.attendance.worker.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link WorkerService} providing worker management operations.
 *
 * <p>Handles CRUD lifecycle, phone-number uniqueness, Redis-based active-worker
 * cache invalidation, and business-rule checks (e.g., preventing deletion of
 * clocked-in workers).</p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class WorkerServiceImpl implements WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceImpl.class);

    private static final String ACTIVE_WORKER_KEY_PREFIX = "active_worker:";

    private final WorkerRepository workerRepository;
    private final WorkerMapper workerMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * {@inheritDoc}
     *
     * @throws DuplicateResourceException if a worker with the same phone already exists
     */
    @Override
    public WorkerResponse createWorker(WorkerCreateRequest request) {
        log.info("Creating worker with name: '{}' and phone: '{}'", request.getName(), request.getPhone());

        if (workerRepository.existsByPhone(request.getPhone())) {
            log.warn("Duplicate phone number detected: '{}'", request.getPhone());
            throw new DuplicateResourceException("Worker", "phone", request.getPhone());
        }

        Worker worker = workerMapper.toEntity(request);
        Worker savedWorker = workerRepository.save(worker);

        log.info("Worker created successfully with id: {}", savedWorker.getId());
        return workerMapper.toResponse(savedWorker);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException  if the worker is not found
     * @throws DuplicateResourceException if the new phone number is already taken
     */
    @Override
    public WorkerResponse updateWorker(Long workerId, WorkerUpdateRequest request) {
        log.info("Updating worker with id: {}", workerId);

        Worker worker = findWorkerOrThrow(workerId);

        // Check phone uniqueness if phone is being changed
        if (request.getPhone() != null && !request.getPhone().equals(worker.getPhone())) {
            if (workerRepository.existsByPhone(request.getPhone())) {
                log.warn("Cannot update worker {} — phone '{}' already in use", workerId, request.getPhone());
                throw new DuplicateResourceException("Worker", "phone", request.getPhone());
            }
            worker.setPhone(request.getPhone());
        }

        // Apply non-null fields
        if (request.getName() != null) {
            worker.setName(request.getName());
        }
        if (request.getDesignation() != null) {
            worker.setDesignation(request.getDesignation());
        }
        if (request.getDailyWageRate() != null) {
            worker.setDailyWageRate(request.getDailyWageRate());
        }

        Worker updatedWorker = workerRepository.save(worker);

        // Invalidate Redis cache if the worker has an active clock-in entry
        invalidateRedisIfActive(workerId);

        log.info("Worker updated successfully with id: {}", updatedWorker.getId());
        return workerMapper.toResponse(updatedWorker);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Performs a soft delete by setting {@code activeStatus} to {@code false}.
     * A worker who is currently clocked in cannot be deleted.</p>
     *
     * @throws ResourceNotFoundException       if the worker is not found
     * @throws BusinessRuleViolationException  if the worker is currently clocked in
     */
    @Override
    public void deleteWorker(Long workerId) {
        log.info("Soft-deleting worker with id: {}", workerId);

        Worker worker = findWorkerOrThrow(workerId);

        // Prevent deletion of a worker who is currently clocked in
        if (isWorkerClockedIn(workerId)) {
            log.warn("Cannot delete worker {} — currently clocked in", workerId);
            throw new BusinessRuleViolationException(
                    "WORKER_CURRENTLY_CLOCKED_IN",
                    "Worker with id " + workerId + " is currently clocked in and cannot be deleted"
            );
        }

        worker.setActiveStatus(false);
        workerRepository.save(worker);

        log.info("Worker soft-deleted successfully with id: {}", workerId);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the worker is not found
     */
    @Override
    @Transactional(readOnly = true)
    public WorkerResponse getWorkerById(Long workerId) {
        log.debug("Fetching worker with id: {}", workerId);

        Worker worker = findWorkerOrThrow(workerId);
        return workerMapper.toResponse(worker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WorkerResponse> getAllWorkers(int page, int size, String sortBy, String sortDir,
                                                       String name, Designation designation, Boolean activeStatus) {
        log.debug("Fetching workers — page: {}, size: {}, sortBy: {}, sortDir: {}, name: {}, designation: {}, activeStatus: {}",
                page, size, sortBy, sortDir, name, designation, activeStatus);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Worker> workerPage = workerRepository.findWithFilters(name, designation, activeStatus, pageable);

        List<WorkerResponse> content = workerMapper.toResponseList(workerPage.getContent());

        return PagedResponse.<WorkerResponse>builder()
                .content(content)
                .page(workerPage.getNumber())
                .size(workerPage.getSize())
                .totalElements(workerPage.getTotalElements())
                .totalPages(workerPage.getTotalPages())
                .last(workerPage.isLast())
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException       if the worker is not found
     * @throws BusinessRuleViolationException  if deactivating a clocked-in worker
     */
    @Override
    public WorkerResponse updateWorkerStatus(Long workerId, WorkerStatusUpdateRequest request) {
        log.info("Updating active status of worker {} to {}", workerId, request.getActiveStatus());

        Worker worker = findWorkerOrThrow(workerId);

        // If deactivating, check for active clock-in
        if (Boolean.FALSE.equals(request.getActiveStatus()) && isWorkerClockedIn(workerId)) {
            log.warn("Cannot deactivate worker {} — currently clocked in", workerId);
            throw new BusinessRuleViolationException(
                    "WORKER_CURRENTLY_CLOCKED_IN",
                    "Worker with id " + workerId + " is currently clocked in and cannot be deactivated"
            );
        }

        worker.setActiveStatus(request.getActiveStatus());
        Worker updatedWorker = workerRepository.save(worker);

        // Invalidate Redis cache if needed
        invalidateRedisIfActive(workerId);

        log.info("Worker {} status updated to {}", workerId, request.getActiveStatus());
        return workerMapper.toResponse(updatedWorker);
    }

    // ======================== Private helpers ========================

    /**
     * Finds a worker by ID or throws {@link ResourceNotFoundException}.
     */
    private Worker findWorkerOrThrow(Long workerId) {
        return workerRepository.findById(workerId)
                .orElseThrow(() -> {
                    log.warn("Worker not found with id: {}", workerId);
                    return new ResourceNotFoundException("Worker", "id", workerId);
                });
    }

    /**
     * Checks whether the given worker currently has an active clock-in entry in Redis.
     */
    private boolean isWorkerClockedIn(Long workerId) {
        String key = ACTIVE_WORKER_KEY_PREFIX + workerId;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Deletes the Redis cache entry for an active worker if it exists.
     */
    private void invalidateRedisIfActive(Long workerId) {
        String key = ACTIVE_WORKER_KEY_PREFIX + workerId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.delete(key);
            log.info("Invalidated Redis cache for worker {}", workerId);
        }
    }
}
