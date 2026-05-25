package com.company.attendance.attendance.service;

import com.company.attendance.attendance.dto.ActiveWorkerResponse;
import com.company.attendance.common.constants.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Dedicated service for managing active worker sessions in Redis.
 * Provides a fast, in-memory lookup layer so that the attendance module
 * can quickly determine which workers are currently clocked-in without
 * querying the relational database.
 */
@Service
public class ActiveWorkerRedisService {

    private static final Logger log = LoggerFactory.getLogger(ActiveWorkerRedisService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public ActiveWorkerRedisService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private String buildKey(Long workerId) {
        return AppConstants.ACTIVE_WORKER_REDIS_PREFIX + workerId;
    }

    /**
     * Registers a worker as active in Redis with a configurable TTL.
     *
     * @param workerId the worker's ID
     * @param data     the active worker session data
     */
    public void addActiveWorker(Long workerId, ActiveWorkerResponse data) {
        String key = buildKey(workerId);
        redisTemplate.opsForValue().set(key, data, AppConstants.ACTIVE_WORKER_TTL_HOURS, TimeUnit.HOURS);
        log.info("Added active worker to Redis: workerId={}, site={}", workerId, data.getSiteName());
    }

    /**
     * Removes a worker's active session from Redis (on clock-out).
     *
     * @param workerId the worker's ID
     */
    public void removeActiveWorker(Long workerId) {
        String key = buildKey(workerId);
        Boolean deleted = redisTemplate.delete(key);
        log.info("Removed active worker from Redis: workerId={}, deleted={}", workerId, deleted);
    }

    /**
     * Retrieves the active session data for a specific worker.
     *
     * @param workerId the worker's ID
     * @return the session data, or {@code null} if the worker is not active
     */
    public ActiveWorkerResponse getActiveWorker(Long workerId) {
        String key = buildKey(workerId);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return objectMapper.convertValue(value, ActiveWorkerResponse.class);
    }

    /**
     * Checks whether a worker is currently clocked-in (has a Redis key).
     *
     * @param workerId the worker's ID
     * @return {@code true} if the worker has an active session
     */
    public boolean isWorkerActive(Long workerId) {
        String key = buildKey(workerId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Retrieves all currently active workers by scanning Redis keys
     * matching the active-worker prefix.
     *
     * @return list of all active worker session records
     */
    public List<ActiveWorkerResponse> getAllActiveWorkers() {
        Set<String> keys = redisTemplate.keys(AppConstants.ACTIVE_WORKER_REDIS_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null) {
            return Collections.emptyList();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(v -> objectMapper.convertValue(v, ActiveWorkerResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * Invalidates the Redis cache entry for a given worker, if present.
     *
     * @param workerId the worker's ID
     */
    public void invalidateWorkerCache(Long workerId) {
        if (isWorkerActive(workerId)) {
            removeActiveWorker(workerId);
            log.info("Invalidated cache for worker: {}", workerId);
        }
    }
}
