package com.company.attendance.overtime.repository;

import com.company.attendance.overtime.entity.OvertimeEntry;
import com.company.attendance.overtime.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link OvertimeEntry} entities.
 */
@Repository
public interface OvertimeEntryRepository extends JpaRepository<OvertimeEntry, Long> {

    /**
     * Finds overtime entries for a specific worker within a date range.
     *
     * @param workerId the worker's ID
     * @param startDate range start date
     * @param endDate range end date
     * @return list of overtime entries
     */
    List<OvertimeEntry> findByWorkerIdAndOvertimeDateBetween(Long workerId, LocalDate startDate, LocalDate endDate);

    /**
     * Finds overtime entries for a specific worker, filtered by settlement status and date range.
     *
     * @param workerId the worker's ID
     * @param settlementStatus the settlement status
     * @param startDate range start date
     * @param endDate range end date
     * @return list of matching overtime entries
     */
    List<OvertimeEntry> findByWorkerIdAndSettlementStatusAndOvertimeDateBetween(
            Long workerId, SettlementStatus settlementStatus, LocalDate startDate, LocalDate endDate);

    /**
     * Calculates the sum of overtime hours logged by a worker during a specific month.
     *
     * @param workerId the worker's ID
     * @param startDate first day of the month
     * @param endDate last day of the month
     * @return total overtime hours, or null if none logged
     */
    @Query("SELECT SUM(o.overtimeHours) FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.overtimeDate BETWEEN :startDate AND :endDate")
    BigDecimal sumOvertimeHoursByWorkerAndMonth(
            @Param("workerId") Long workerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
