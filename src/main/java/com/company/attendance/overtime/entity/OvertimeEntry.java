package com.company.attendance.overtime.entity;

import com.company.attendance.attendance.entity.AttendanceLog;
import com.company.attendance.common.entity.BaseEntity;
import com.company.attendance.worker.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing an overtime record for a specific worker and shift.
 */
@Entity
@Table(name = "overtime_entries", indexes = {
        @Index(name = "idx_overtime_worker_id", columnList = "worker_id"),
        @Index(name = "idx_overtime_date", columnList = "overtime_date"),
        @Index(name = "idx_overtime_settlement", columnList = "settlement_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_log_id", nullable = false)
    private AttendanceLog attendanceLog;

    @Column(name = "overtime_date", nullable = false)
    private LocalDate overtimeDate;

    @Column(name = "overtime_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "overtime_rate_applied", nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeRateApplied;

    @Column(name = "overtime_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 20)
    @Builder.Default
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;
}
