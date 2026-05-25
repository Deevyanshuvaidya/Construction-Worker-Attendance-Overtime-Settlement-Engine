package com.company.attendance.attendance.entity;

import com.company.attendance.common.entity.BaseEntity;
import com.company.attendance.site.entity.Site;
import com.company.attendance.worker.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a single attendance clock-in/clock-out record for a worker at a site.
 * Tracks total hours worked, overtime hours, and whether the record has been flagged for review.
 */
@Entity
@Table(name = "attendance_logs", indexes = {
        @Index(name = "idx_attendance_worker_id", columnList = "worker_id"),
        @Index(name = "idx_attendance_clock_in", columnList = "clock_in_timestamp"),
        @Index(name = "idx_attendance_worker_clock_in", columnList = "worker_id, clock_in_timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "clock_in_timestamp", nullable = false)
    private LocalDateTime clockInTimestamp;

    @Column(name = "clock_out_timestamp")
    private LocalDateTime clockOutTimestamp;

    @Column(name = "total_hours_worked", precision = 5, scale = 2)
    private BigDecimal totalHoursWorked;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "flagged_for_review", nullable = false)
    @Builder.Default
    private Boolean flaggedForReview = false;
}
