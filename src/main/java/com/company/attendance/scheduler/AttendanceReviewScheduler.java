package com.company.attendance.scheduler;

import com.company.attendance.attendance.entity.AttendanceLog;
import com.company.attendance.attendance.repository.AttendanceLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for checking and flagging stale open attendance logs.
 * Runs periodically to scan the database.
 */
@Component
@RequiredArgsConstructor
public class AttendanceReviewScheduler {

    private static final Logger log = LoggerFactory.getLogger(AttendanceReviewScheduler.class);

    private final AttendanceLogRepository attendanceLogRepository;

    /**
     * Periodically runs every hour to scan for attendance sessions clocked in over 16 hours ago
     * without an active clock-out, flagging them for manual review.
     */
    @Scheduled(fixedRate = 3600000) // Runs every hour (3,600,000 milliseconds)
    @Transactional
    public void scanAndFlagStaleAttendance() {
        log.info("Starting scheduled stale attendance scan...");

        // Cutoff threshold is 16 hours ago
        LocalDateTime threshold = LocalDateTime.now().minusHours(16);

        List<AttendanceLog> staleLogs = attendanceLogRepository.findStaleOpenAttendance(threshold);

        if (staleLogs.isEmpty()) {
            log.info("Stale attendance scan completed: no stale records found.");
            return;
        }

        log.warn("Found {} stale attendance sessions clocked in before {}! Flagging for review.", staleLogs.size(), threshold);

        for (AttendanceLog logItem : staleLogs) {
            logItem.setFlaggedForReview(true);
            log.warn("Flagged attendance log: ID={}, workerId='{}', siteId='{}', clockInTime={}",
                    logItem.getId(),
                    logItem.getWorker().getId(),
                    logItem.getSite().getId(),
                    logItem.getClockInTimestamp());
        }

        attendanceLogRepository.saveAll(staleLogs);
        log.info("Successfully flagged and saved {} stale attendance records for review.", staleLogs.size());
    }
}
