package com.company.attendance.attendance.repository;

import com.company.attendance.attendance.entity.AttendanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AttendanceLog} entities.
 * Provides custom queries for open attendance lookups, filtered searches, and stale record detection.
 */
@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    /**
     * Finds the currently open (no clock-out) attendance log for a given worker.
     *
     * @param workerId the worker's ID
     * @return the open attendance log, if any
     */
    Optional<AttendanceLog> findByWorkerIdAndClockOutTimestampIsNull(Long workerId);

    /**
     * Checks whether a worker currently has an open attendance session.
     *
     * @param workerId the worker's ID
     * @return {@code true} if the worker has an open session
     */
    boolean existsByWorkerIdAndClockOutTimestampIsNull(Long workerId);

    /**
     * Paginated query with optional filters for worker ID and time range.
     * Eager-fetches worker and site associations to avoid N+1 queries.
     *
     * @param workerId optional worker filter
     * @param from     optional start of time range (inclusive)
     * @param to       optional end of time range (inclusive)
     * @param pageable pagination and sorting parameters
     * @return paginated attendance logs matching the filters
     */
    @Query("SELECT a FROM AttendanceLog a JOIN FETCH a.worker JOIN FETCH a.site " +
           "WHERE (:workerId IS NULL OR a.worker.id = :workerId) " +
           "AND (:from IS NULL OR a.clockInTimestamp >= :from) " +
           "AND (:to IS NULL OR a.clockInTimestamp <= :to)")
    Page<AttendanceLog> findWithFilters(
            @Param("workerId") Long workerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    /**
     * Finds open attendance records where the clock-in occurred before the given threshold,
     * indicating a potentially stale session that should be flagged for review.
     *
     * @param threshold the cutoff timestamp
     * @return stale open attendance logs
     */
    @Query("SELECT a FROM AttendanceLog a WHERE a.clockOutTimestamp IS NULL " +
           "AND a.clockInTimestamp < :threshold")
    List<AttendanceLog> findStaleOpenAttendance(@Param("threshold") LocalDateTime threshold);
}
