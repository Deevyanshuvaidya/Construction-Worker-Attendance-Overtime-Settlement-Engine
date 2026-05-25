package com.company.attendance.attendance.service;

import com.company.attendance.attendance.dto.*;
import com.company.attendance.attendance.entity.AttendanceLog;
import com.company.attendance.attendance.mapper.AttendanceMapper;
import com.company.attendance.attendance.repository.AttendanceLogRepository;
import com.company.attendance.common.exception.BusinessRuleViolationException;
import com.company.attendance.common.exception.ResourceNotFoundException;
import com.company.attendance.overtime.dto.OvertimeCalculationResult;
import com.company.attendance.overtime.entity.OvertimeEntry;
import com.company.attendance.overtime.repository.OvertimeEntryRepository;
import com.company.attendance.overtime.service.OvertimeCalculationEngine;
import com.company.attendance.site.entity.Site;
import com.company.attendance.site.repository.SiteRepository;
import com.company.attendance.worker.entity.Designation;
import com.company.attendance.worker.entity.Worker;
import com.company.attendance.worker.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AttendanceServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceLogRepository attendanceLogRepository;

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private OvertimeEntryRepository overtimeEntryRepository;

    @Mock
    private OvertimeCalculationEngine overtimeCalculationEngine;

    @Mock
    private ActiveWorkerRedisService activeWorkerRedisService;

    @Mock
    private AttendanceMapper attendanceMapper;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @Captor
    private ArgumentCaptor<AttendanceLog> attendanceLogCaptor;

    @Captor
    private ArgumentCaptor<OvertimeEntry> overtimeEntryCaptor;

    private Worker activeWorker;
    private Worker inactiveWorker;
    private Site activeSite;
    private Site inactiveSite;
    private ClockInRequest clockInRequest;
    private AttendanceLogResponse attendanceLogResponse;

    @BeforeEach
    void setUp() {
        activeWorker = new Worker();
        activeWorker.setId(1L);
        activeWorker.setName("Ramesh Kumar");
        activeWorker.setPhone("9876543210");
        activeWorker.setDesignation(Designation.MASON);
        activeWorker.setDailyWageRate(new BigDecimal("1200.00"));
        activeWorker.setActiveStatus(true);

        inactiveWorker = new Worker();
        inactiveWorker.setId(2L);
        inactiveWorker.setName("Suresh Inactive");
        inactiveWorker.setActiveStatus(false);

        activeSite = new Site();
        activeSite.setId(1L);
        activeSite.setSiteName("Greenfield Towers");
        activeSite.setLocation("Whitefield, Bangalore");
        activeSite.setActiveStatus(true);

        inactiveSite = new Site();
        inactiveSite.setId(2L);
        inactiveSite.setSiteName("Old Project");
        inactiveSite.setActiveStatus(false);

        clockInRequest = new ClockInRequest(1L, 1L);

        attendanceLogResponse = AttendanceLogResponse.builder()
                .id(1L)
                .workerId(1L)
                .siteId(1L)
                .build();
    }

    // ==================== Clock In Tests ====================

    @Test
    @DisplayName("Should clock in worker successfully when worker and site are active")
    void clockIn_Success() {
        // Given
        when(workerRepository.findById(1L)).thenReturn(Optional.of(activeWorker));
        when(siteRepository.findById(1L)).thenReturn(Optional.of(activeSite));
        when(activeWorkerRedisService.isWorkerActive(1L)).thenReturn(false);
        when(attendanceLogRepository.existsByWorkerIdAndClockOutTimestampIsNull(1L)).thenReturn(false);
        when(attendanceLogRepository.save(any(AttendanceLog.class))).thenAnswer(invocation -> {
            AttendanceLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });
        when(attendanceMapper.toResponse(any(AttendanceLog.class))).thenReturn(attendanceLogResponse);

        // When
        AttendanceLogResponse result = attendanceService.clockIn(clockInRequest);

        // Then
        assertNotNull(result);
        verify(workerRepository).findById(1L);
        verify(siteRepository).findById(1L);
        verify(activeWorkerRedisService).isWorkerActive(1L);
        verify(attendanceLogRepository).save(attendanceLogCaptor.capture());
        verify(activeWorkerRedisService).addActiveWorker(eq(1L), any(ActiveWorkerResponse.class));

        AttendanceLog savedLog = attendanceLogCaptor.getValue();
        assertNotNull(savedLog.getClockInTimestamp());
        assertNull(savedLog.getClockOutTimestamp());
        assertEquals(activeWorker, savedLog.getWorker());
        assertEquals(activeSite, savedLog.getSite());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when worker does not exist")
    void clockIn_WorkerNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(workerRepository.findById(99L)).thenReturn(Optional.empty());
        ClockInRequest request = new ClockInRequest(99L, 1L);

        // When & Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> attendanceService.clockIn(request)
        );
        verify(attendanceLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolationException when worker is inactive")
    void clockIn_WorkerInactive_ThrowsBusinessRuleViolationException() {
        // Given
        when(workerRepository.findById(2L)).thenReturn(Optional.of(inactiveWorker));
        ClockInRequest request = new ClockInRequest(2L, 1L);

        // When & Then
        assertThrows(
                BusinessRuleViolationException.class,
                () -> attendanceService.clockIn(request)
        );
        verify(attendanceLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolationException when site is inactive")
    void clockIn_SiteInactive_ThrowsBusinessRuleViolationException() {
        // Given
        when(workerRepository.findById(1L)).thenReturn(Optional.of(activeWorker));
        when(siteRepository.findById(2L)).thenReturn(Optional.of(inactiveSite));
        ClockInRequest request = new ClockInRequest(1L, 2L);

        // When & Then
        assertThrows(
                BusinessRuleViolationException.class,
                () -> attendanceService.clockIn(request)
        );
        verify(attendanceLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolationException when worker is already clocked in")
    void clockIn_AlreadyClockedIn_ThrowsBusinessRuleViolationException() {
        // Given
        when(workerRepository.findById(1L)).thenReturn(Optional.of(activeWorker));
        when(siteRepository.findById(1L)).thenReturn(Optional.of(activeSite));
        when(activeWorkerRedisService.isWorkerActive(1L)).thenReturn(true);

        // When & Then
        assertThrows(
                BusinessRuleViolationException.class,
                () -> attendanceService.clockIn(clockInRequest)
        );
        verify(attendanceLogRepository, never()).save(any());
    }

    // ==================== Clock Out Tests ====================

    @Test
    @DisplayName("Should clock out successfully with no overtime for 6-hour shift")
    void clockOut_Success_NoOvertime() {
        // Given
        LocalDateTime clockIn = LocalDateTime.now().minusHours(6);

        AttendanceLog existingLog = new AttendanceLog();
        existingLog.setId(1L);
        existingLog.setWorker(activeWorker);
        existingLog.setSite(activeSite);
        existingLog.setClockInTimestamp(clockIn);

        ClockOutRequest clockOutRequest = new ClockOutRequest(1L);

        when(attendanceLogRepository.findByWorkerIdAndClockOutTimestampIsNull(1L))
                .thenReturn(Optional.of(existingLog));
        when(attendanceLogRepository.save(any(AttendanceLog.class))).thenAnswer(i -> i.getArgument(0));
        when(attendanceMapper.toResponse(any(AttendanceLog.class))).thenReturn(attendanceLogResponse);

        // When
        AttendanceLogResponse result = attendanceService.clockOut(clockOutRequest);

        // Then
        assertNotNull(result);
        verify(attendanceLogRepository).save(attendanceLogCaptor.capture());
        verify(overtimeEntryRepository, never()).save(any(OvertimeEntry.class));
        verify(activeWorkerRedisService).removeActiveWorker(1L);

        AttendanceLog savedLog = attendanceLogCaptor.getValue();
        assertNotNull(savedLog.getClockOutTimestamp());
    }

    @Test
    @DisplayName("Should clock out successfully and create overtime entry for 10-hour shift")
    void clockOut_Success_WithOvertime() {
        // Given
        LocalDateTime clockIn = LocalDateTime.now().minusHours(10);

        AttendanceLog existingLog = new AttendanceLog();
        existingLog.setId(1L);
        existingLog.setWorker(activeWorker);
        existingLog.setSite(activeSite);
        existingLog.setClockInTimestamp(clockIn);

        ClockOutRequest clockOutRequest = new ClockOutRequest(1L);

        when(attendanceLogRepository.findByWorkerIdAndClockOutTimestampIsNull(1L))
                .thenReturn(Optional.of(existingLog));
        when(overtimeEntryRepository.sumOvertimeHoursByWorkerAndMonth(eq(1L), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        OvertimeCalculationEngine.OvertimeResult otResult = new OvertimeCalculationEngine.OvertimeResult(
                new BigDecimal("2.00"),
                new BigDecimal("450.00"),
                new BigDecimal("1.50"),
                false
        );
        when(overtimeCalculationEngine.calculate(any(), any(), any())).thenReturn(otResult);
        when(attendanceLogRepository.save(any(AttendanceLog.class))).thenAnswer(i -> i.getArgument(0));
        when(overtimeEntryRepository.save(any(OvertimeEntry.class))).thenAnswer(i -> i.getArgument(0));
        when(attendanceMapper.toResponse(any(AttendanceLog.class))).thenReturn(attendanceLogResponse);

        // When
        AttendanceLogResponse result = attendanceService.clockOut(clockOutRequest);

        // Then
        assertNotNull(result);
        verify(overtimeEntryRepository).save(overtimeEntryCaptor.capture());
        verify(activeWorkerRedisService).removeActiveWorker(1L);

        OvertimeEntry savedEntry = overtimeEntryCaptor.getValue();
        assertEquals(0, new BigDecimal("2.00").compareTo(savedEntry.getOvertimeHours()));
        assertEquals(0, new BigDecimal("450.00").compareTo(savedEntry.getOvertimeAmount()));
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolationException when worker is not clocked in during clock out")
    void clockOut_NotClockedIn_ThrowsBusinessRuleViolationException() {
        // Given
        ClockOutRequest clockOutRequest = new ClockOutRequest(1L);

        when(attendanceLogRepository.findByWorkerIdAndClockOutTimestampIsNull(1L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                BusinessRuleViolationException.class,
                () -> attendanceService.clockOut(clockOutRequest)
        );
        verify(attendanceLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should flag attendance for review when shift exceeds 16 hours")
    void clockOut_FlaggedForReview_Over16Hours() {
        // Given
        LocalDateTime clockIn = LocalDateTime.now().minusHours(17);

        AttendanceLog existingLog = new AttendanceLog();
        existingLog.setId(1L);
        existingLog.setWorker(activeWorker);
        existingLog.setSite(activeSite);
        existingLog.setClockInTimestamp(clockIn);

        ClockOutRequest clockOutRequest = new ClockOutRequest(1L);

        when(attendanceLogRepository.findByWorkerIdAndClockOutTimestampIsNull(1L))
                .thenReturn(Optional.of(existingLog));
        when(overtimeEntryRepository.sumOvertimeHoursByWorkerAndMonth(eq(1L), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        OvertimeCalculationEngine.OvertimeResult otResult = new OvertimeCalculationEngine.OvertimeResult(
                new BigDecimal("8.00"),
                new BigDecimal("2400.00"),
                new BigDecimal("2.00"),
                false
        );
        when(overtimeCalculationEngine.calculate(any(), any(), any())).thenReturn(otResult);
        when(attendanceLogRepository.save(any(AttendanceLog.class))).thenAnswer(i -> i.getArgument(0));
        when(overtimeEntryRepository.save(any(OvertimeEntry.class))).thenAnswer(i -> i.getArgument(0));
        when(attendanceMapper.toResponse(any(AttendanceLog.class))).thenReturn(attendanceLogResponse);

        // When
        attendanceService.clockOut(clockOutRequest);

        // Then
        verify(attendanceLogRepository).save(attendanceLogCaptor.capture());
        AttendanceLog savedLog = attendanceLogCaptor.getValue();
        assertTrue(savedLog.getFlaggedForReview(),
                "Attendance log should be flagged for review when shift exceeds 16 hours");
    }

    // ==================== Active Workers ====================

    @Test
    @DisplayName("Should return active workers from Redis")
    void getActiveWorkers_ReturnsFromRedis() {
        // Given
        ActiveWorkerResponse activeResponse = ActiveWorkerResponse.builder()
                .workerId(1L)
                .workerName("Ramesh Kumar")
                .siteId(1L)
                .siteName("Greenfield Towers")
                .clockInTime(LocalDateTime.now())
                .build();
        when(activeWorkerRedisService.getAllActiveWorkers()).thenReturn(List.of(activeResponse));

        // When
        List<ActiveWorkerResponse> result = attendanceService.getActiveWorkers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(activeWorkerRedisService).getAllActiveWorkers();
    }
}
