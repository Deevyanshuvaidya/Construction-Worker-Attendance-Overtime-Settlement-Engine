package com.company.attendance.overtime.service;

import com.company.attendance.common.exception.BusinessRuleViolationException;
import com.company.attendance.common.exception.ResourceNotFoundException;
import com.company.attendance.overtime.dto.OvertimeEntryResponseDTO;
import com.company.attendance.overtime.dto.OvertimeSummaryDTO;
import com.company.attendance.overtime.entity.OvertimeEntry;
import com.company.attendance.overtime.entity.SettlementStatus;
import com.company.attendance.overtime.mapper.OvertimeMapper;
import com.company.attendance.overtime.repository.OvertimeEntryRepository;
import com.company.attendance.worker.entity.Designation;
import com.company.attendance.worker.entity.Worker;
import com.company.attendance.worker.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OvertimeServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class OvertimeServiceImplTest {

    @Mock
    private OvertimeEntryRepository overtimeEntryRepository;

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private OvertimeMapper overtimeMapper;

    @InjectMocks
    private OvertimeServiceImpl overtimeService;

    private Worker worker;
    private OvertimeEntry pendingEntry;
    private OvertimeEntry settledEntry;

    @BeforeEach
    void setUp() {
        worker = new Worker();
        worker.setId(1L);
        worker.setName("Ramesh Kumar");
        worker.setPhone("9876543210");
        worker.setDesignation(Designation.MASON);
        worker.setDailyWageRate(new BigDecimal("1200.00"));
        worker.setActiveStatus(true);

        pendingEntry = new OvertimeEntry();
        pendingEntry.setId(1L);
        pendingEntry.setWorker(worker);
        pendingEntry.setOvertimeDate(LocalDate.now().minusMonths(1).withDayOfMonth(15));
        pendingEntry.setOvertimeHours(new BigDecimal("2.00"));
        pendingEntry.setOvertimeRateApplied(new BigDecimal("1.5"));
        pendingEntry.setOvertimeAmount(new BigDecimal("450.00"));
        pendingEntry.setSettlementStatus(SettlementStatus.PENDING);

        settledEntry = new OvertimeEntry();
        settledEntry.setId(2L);
        settledEntry.setWorker(worker);
        settledEntry.setOvertimeDate(LocalDate.now().minusMonths(2).withDayOfMonth(10));
        settledEntry.setOvertimeHours(new BigDecimal("3.00"));
        settledEntry.setOvertimeRateApplied(new BigDecimal("1.5"));
        settledEntry.setOvertimeAmount(new BigDecimal("675.00"));
        settledEntry.setSettlementStatus(SettlementStatus.SETTLED);
    }

    @Test
    @DisplayName("Should return overtime summary for worker successfully")
    void getOvertimeSummary_Success() {
        // Given
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue() - 1;
        if (month == 0) {
            month = 12;
            year = year - 1;
        }

        List<OvertimeEntry> entries = List.of(pendingEntry);
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(overtimeEntryRepository.findByWorkerIdAndOvertimeDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(entries);

        OvertimeEntryResponseDTO entryDTO = OvertimeEntryResponseDTO.builder()
                .id(1L)
                .workerId(1L)
                .overtimeHours(new BigDecimal("2.00"))
                .overtimeAmount(new BigDecimal("450.00"))
                .settlementStatus(SettlementStatus.PENDING)
                .build();
        when(overtimeMapper.toResponseDTO(any(OvertimeEntry.class))).thenReturn(entryDTO);

        // When
        OvertimeSummaryDTO result = overtimeService.getOvertimeSummary(1L, year, month);

        // Then
        assertNotNull(result);
        assertFalse(result.getEntries().isEmpty());
        verify(workerRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty summary when no overtime entries exist")
    void getOvertimeSummary_NoEntries_ReturnsEmpty() {
        // Given
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(overtimeEntryRepository.findByWorkerIdAndOvertimeDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        OvertimeSummaryDTO result = overtimeService.getOvertimeSummary(1L, year, month);

        // Then
        assertNotNull(result);
        assertTrue(result.getEntries().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalOvertimeHours()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalOvertimeAmount()));
    }

    @Test
    @DisplayName("Should settle overtime entries successfully for past month")
    void settleOvertime_Success() {
        // Given
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int year = lastMonth.getYear();
        int month = lastMonth.getMonthValue();

        List<OvertimeEntry> pendingEntries = List.of(pendingEntry);
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(overtimeEntryRepository.findByWorkerIdAndSettlementStatusAndOvertimeDateBetween(
                eq(1L), eq(SettlementStatus.PENDING), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(pendingEntries);

        // When
        overtimeService.settleOvertime(1L, year, month);

        // Then
        verify(overtimeEntryRepository).saveAll(anyList());
        assertEquals(SettlementStatus.SETTLED, pendingEntry.getSettlementStatus());
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolationException when settling current month overtime")
    void settleOvertime_CurrentMonth_ThrowsBusinessRuleViolationException() {
        // Given
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));

        // When & Then
        assertThrows(
                BusinessRuleViolationException.class,
                () -> overtimeService.settleOvertime(1L, currentYear, currentMonth)
        );
        verify(overtimeEntryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no entries found for settlement")
    void settleOvertime_NoEntries_ThrowsResourceNotFoundException() {
        // Given
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int year = lastMonth.getYear();
        int month = lastMonth.getMonthValue();

        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(overtimeEntryRepository.findByWorkerIdAndSettlementStatusAndOvertimeDateBetween(
                eq(1L), eq(SettlementStatus.PENDING), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> overtimeService.settleOvertime(1L, year, month)
        );
        verify(overtimeEntryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when all entries are already settled")
    void settleOvertime_AlreadySettled_ThrowsResourceNotFoundException() {
        // Given
        LocalDate pastMonth = LocalDate.now().minusMonths(2);
        int year = pastMonth.getYear();
        int month = pastMonth.getMonthValue();

        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        // No PENDING entries — they're all already SETTLED
        when(overtimeEntryRepository.findByWorkerIdAndSettlementStatusAndOvertimeDateBetween(
                eq(1L), eq(SettlementStatus.PENDING), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> overtimeService.settleOvertime(1L, year, month)
        );
        verify(overtimeEntryRepository, never()).saveAll(any());
    }
}
