package com.company.attendance.attendance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for attendance log records, including worker and site details,
 * timestamps, hours worked, overtime, and review flag status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceLogResponse {

    private Long id;
    private Long workerId;
    private String workerName;
    private Long siteId;
    private String siteName;
    private LocalDateTime clockInTimestamp;
    private LocalDateTime clockOutTimestamp;
    private BigDecimal totalHoursWorked;
    private BigDecimal overtimeHours;
    private Boolean flaggedForReview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
