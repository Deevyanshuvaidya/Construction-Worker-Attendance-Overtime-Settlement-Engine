package com.company.attendance.overtime.service;

import com.company.attendance.common.exception.BusinessRuleViolationException;
import com.company.attendance.common.exception.ResourceNotFoundException;
import com.company.attendance.overtime.dto.OvertimeEntryResponseDTO;
import com.company.attendance.overtime.dto.OvertimeSummaryDTO;
import com.company.attendance.overtime.entity.OvertimeEntry;
import com.company.attendance.overtime.entity.SettlementStatus;
import com.company.attendance.overtime.mapper.OvertimeMapper;
import com.company.attendance.overtime.repository.OvertimeEntryRepository;
import com.company.attendance.worker.entity.Worker;
import com.company.attendance.worker.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for managing worker overtime calculations and settlements.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class OvertimeServiceImpl implements OvertimeService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeServiceImpl.class);

    private final OvertimeEntryRepository overtimeEntryRepository;
    private final WorkerRepository workerRepository;
    private final OvertimeMapper overtimeMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public OvertimeSummaryDTO getOvertimeSummary(Long workerId, int year, int month) {
        log.info("Fetching overtime summary for workerId={}, year={}, month={}", workerId, year, month);

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", workerId));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<OvertimeEntry> entries = overtimeEntryRepository.findByWorkerIdAndOvertimeDateBetween(workerId, start, end);

        BigDecimal totalHours = entries.stream()
                .map(OvertimeEntry::getOvertimeHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = entries.stream()
                .map(OvertimeEntry::getOvertimeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OvertimeEntryResponseDTO> entryDTOs = entries.stream()
                .map(overtimeMapper::toResponseDTO)
                .toList();

        boolean wasCapped = totalHours.compareTo(new BigDecimal("60")) >= 0;

        return OvertimeSummaryDTO.builder()
                .workerId(worker.getId())
                .workerName(worker.getName())
                .year(year)
                .month(month)
                .totalOvertimeHours(totalHours)
                .totalOvertimeAmount(totalAmount)
                .entries(entryDTOs)
                .wasCapped(wasCapped)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void settleOvertime(Long workerId, int year, int month) {
        log.info("Initiating overtime settlement for workerId={}, year={}, month={}", workerId, year, month);

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", workerId));

        // Block settlement of the current month
        LocalDate today = LocalDate.now();
        if (year == today.getYear() && month == today.getMonthValue()) {
            log.warn("Attempted to settle overtime for the current month: workerId={}, year={}, month={}", workerId, year, month);
            throw new BusinessRuleViolationException(
                    "CANNOT_SETTLE_CURRENT_MONTH",
                    "Overtime for the current month cannot be settled until the month is complete"
            );
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        // Fetch all PENDING entries for this month
        List<OvertimeEntry> pendingEntries = overtimeEntryRepository
                .findByWorkerIdAndSettlementStatusAndOvertimeDateBetween(workerId, SettlementStatus.PENDING, start, end);

        if (pendingEntries.isEmpty()) {
            log.warn("No pending overtime entries found for settlement: workerId={}, year={}, month={}", workerId, year, month);
            throw new ResourceNotFoundException("Pending Overtime Entries", "workerId/month", workerId + "/" + month);
        }

        // Mark all as settled
        pendingEntries.forEach(entry -> entry.setSettlementStatus(SettlementStatus.SETTLED));

        overtimeEntryRepository.saveAll(pendingEntries);

        log.info("Successfully settled {} overtime entries for workerId={}, year={}, month={}", pendingEntries.size(), workerId, year, month);
    }
}
