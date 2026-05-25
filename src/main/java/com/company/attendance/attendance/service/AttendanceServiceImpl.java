package com.company.attendance.attendance.service;

import com.company.attendance.attendance.dto.*;
import com.company.attendance.attendance.entity.AttendanceLog;
import com.company.attendance.attendance.mapper.AttendanceMapper;
import com.company.attendance.attendance.repository.AttendanceLogRepository;
import com.company.attendance.common.constants.AppConstants;
import com.company.attendance.common.dto.PagedResponse;
import com.company.attendance.common.exception.BusinessRuleViolationException;
import com.company.attendance.common.exception.ResourceNotFoundException;
import com.company.attendance.overtime.entity.OvertimeEntry;
import com.company.attendance.overtime.entity.SettlementStatus;
import com.company.attendance.overtime.repository.OvertimeEntryRepository;
import com.company.attendance.overtime.service.OvertimeCalculationEngine;
import com.company.attendance.site.entity.Site;
import com.company.attendance.site.repository.SiteRepository;
import com.company.attendance.worker.entity.Worker;
import com.company.attendance.worker.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Core attendance service implementing clock-in/clock-out workflows with
 * integrated overtime calculation, Redis caching, and review-flagging logic.
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    private final AttendanceLogRepository attendanceLogRepository;
    private final WorkerRepository workerRepository;
    private final SiteRepository siteRepository;
    private final OvertimeEntryRepository overtimeEntryRepository;
    private final OvertimeCalculationEngine overtimeCalculationEngine;
    private final ActiveWorkerRedisService activeWorkerRedisService;
    private final AttendanceMapper attendanceMapper;

    public AttendanceServiceImpl(AttendanceLogRepository attendanceLogRepository,
                                  WorkerRepository workerRepository,
                                  SiteRepository siteRepository,
                                  OvertimeEntryRepository overtimeEntryRepository,
                                  OvertimeCalculationEngine overtimeCalculationEngine,
                                  ActiveWorkerRedisService activeWorkerRedisService,
                                  AttendanceMapper attendanceMapper) {
        this.attendanceLogRepository = attendanceLogRepository;
        this.workerRepository = workerRepository;
        this.siteRepository = siteRepository;
        this.overtimeEntryRepository = overtimeEntryRepository;
        this.overtimeCalculationEngine = overtimeCalculationEngine;
        this.activeWorkerRedisService = activeWorkerRedisService;
        this.attendanceMapper = attendanceMapper;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Validates the worker and site exist and are active, checks for duplicate
     * clock-in (Redis-first, DB-fallback), creates the attendance record,
     * and registers the session in Redis.
     */
    @Override
    @Transactional
    public AttendanceLogResponse clockIn(ClockInRequest request) {
        log.info("Processing clock-in: workerId={}, siteId={}", request.getWorkerId(), request.getSiteId());

        // 1. Validate worker exists and is active
        Worker worker = workerRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", request.getWorkerId()));

        if (!worker.getActiveStatus()) {
            throw new BusinessRuleViolationException("WORKER_INACTIVE", "Worker is not active");
        }

        // 2. Validate site exists and is active
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site", "id", request.getSiteId()));

        if (!site.getActiveStatus()) {
            throw new BusinessRuleViolationException("SITE_INACTIVE", "Site is not active");
        }

        // 3. Check for duplicate clock-in (Redis first, then DB fallback)
        if (activeWorkerRedisService.isWorkerActive(request.getWorkerId())) {
            throw new BusinessRuleViolationException("DUPLICATE_CLOCK_IN", "Worker is already clocked in");
        }

        if (attendanceLogRepository.existsByWorkerIdAndClockOutTimestampIsNull(request.getWorkerId())) {
            throw new BusinessRuleViolationException("DUPLICATE_CLOCK_IN", "Worker is already clocked in");
        }

        // 4. Create attendance log
        LocalDateTime now = LocalDateTime.now();
        AttendanceLog attendanceLog = AttendanceLog.builder()
                .worker(worker)
                .site(site)
                .clockInTimestamp(now)
                .flaggedForReview(false)
                .build();

        attendanceLog = attendanceLogRepository.save(attendanceLog);

        // 5. Add to Redis
        ActiveWorkerResponse activeData = ActiveWorkerResponse.builder()
                .workerId(worker.getId())
                .workerName(worker.getName())
                .siteId(site.getId())
                .siteName(site.getSiteName())
                .clockInTime(now)
                .build();
        activeWorkerRedisService.addActiveWorker(worker.getId(), activeData);

        log.info("Clock-in successful: workerId={}, siteId={}, timestamp={}", worker.getId(), site.getId(), now);
        return attendanceMapper.toResponse(attendanceLog);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Finds the open attendance record, calculates total hours and overtime
     * using the tiered overtime engine, creates an overtime entry if applicable,
     * flags shifts exceeding the maximum allowed duration, and removes the
     * worker's session from Redis.
     */
    @Override
    @Transactional
    public AttendanceLogResponse clockOut(ClockOutRequest request) {
        log.info("Processing clock-out: workerId={}", request.getWorkerId());

        // 1. Find open attendance record
        AttendanceLog attendanceLog = attendanceLogRepository
                .findByWorkerIdAndClockOutTimestampIsNull(request.getWorkerId())
                .orElseThrow(() -> new BusinessRuleViolationException("NOT_CLOCKED_IN",
                        "Worker is not currently clocked in"));

        // 2. Set clock-out time and calculate hours
        LocalDateTime clockOut = LocalDateTime.now();
        attendanceLog.setClockOutTimestamp(clockOut);

        Duration duration = Duration.between(attendanceLog.getClockInTimestamp(), clockOut);
        BigDecimal totalHours = BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        attendanceLog.setTotalHoursWorked(totalHours);

        // 3. Flag for review if shift exceeds 16 hours
        if (totalHours.compareTo(AppConstants.MAX_SHIFT_HOURS) > 0) {
            attendanceLog.setFlaggedForReview(true);
            log.warn("Shift exceeds 16 hours, flagged for review: workerId={}, hours={}",
                    request.getWorkerId(), totalHours);
        }

        // 4. Calculate overtime
        BigDecimal overtimeHours = totalHours.subtract(AppConstants.STANDARD_SHIFT_HOURS)
                .max(BigDecimal.ZERO);
        attendanceLog.setOvertimeHours(overtimeHours);

        // 5. Create overtime entry if applicable
        if (overtimeHours.compareTo(BigDecimal.ZERO) > 0) {
            Worker worker = attendanceLog.getWorker();

            // Get existing monthly overtime hours for cap calculation
            LocalDate overtimeDate = attendanceLog.getClockInTimestamp().toLocalDate();
            YearMonth yearMonth = YearMonth.from(overtimeDate);
            BigDecimal existingMonthlyOT = overtimeEntryRepository
                    .sumOvertimeHoursByWorkerAndMonth(
                            worker.getId(),
                            yearMonth.atDay(1),
                            yearMonth.atEndOfMonth());
            if (existingMonthlyOT == null) {
                existingMonthlyOT = BigDecimal.ZERO;
            }

            // Use the overtime calculation engine
            OvertimeCalculationEngine.OvertimeResult result = overtimeCalculationEngine.calculate(
                    totalHours, worker.getDailyWageRate(), existingMonthlyOT);

            // Update the attendance log with capped overtime
            attendanceLog.setOvertimeHours(result.getEffectiveOvertimeHours());

            if (result.getEffectiveOvertimeHours().compareTo(BigDecimal.ZERO) > 0) {
                OvertimeEntry entry = OvertimeEntry.builder()
                        .worker(worker)
                        .attendanceLog(attendanceLog)
                        .overtimeDate(overtimeDate)
                        .overtimeHours(result.getEffectiveOvertimeHours())
                        .overtimeRateApplied(result.getWeightedAverageRate())
                        .overtimeAmount(result.getTotalAmount())
                        .settlementStatus(SettlementStatus.PENDING)
                        .build();
                overtimeEntryRepository.save(entry);
                log.info("Overtime entry created: workerId={}, hours={}, amount={}",
                        worker.getId(), result.getEffectiveOvertimeHours(), result.getTotalAmount());
            }

            if (result.isWasCapped()) {
                log.warn("Monthly overtime cap reached for workerId={}, month={}",
                        worker.getId(), yearMonth);
            }
        }

        attendanceLogRepository.save(attendanceLog);

        // 6. Remove from Redis
        activeWorkerRedisService.removeActiveWorker(request.getWorkerId());

        log.info("Clock-out successful: workerId={}, totalHours={}, overtimeHours={}",
                request.getWorkerId(), totalHours, attendanceLog.getOvertimeHours());
        return attendanceMapper.toResponse(attendanceLog);
    }

    /** {@inheritDoc} */
    @Override
    public List<ActiveWorkerResponse> getActiveWorkers() {
        log.debug("Fetching active workers from Redis");
        return activeWorkerRedisService.getAllActiveWorkers();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AttendanceLogResponse> getAttendanceLogs(Long workerId, LocalDateTime from,
                                                                    LocalDateTime to, int page, int size,
                                                                    String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AttendanceLog> logPage = attendanceLogRepository.findWithFilters(workerId, from, to, pageable);

        List<AttendanceLogResponse> content = logPage.getContent().stream()
                .map(attendanceMapper::toResponse)
                .toList();

        return PagedResponse.<AttendanceLogResponse>builder()
                .content(content)
                .page(logPage.getNumber())
                .size(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .last(logPage.isLast())
                .build();
    }
}
