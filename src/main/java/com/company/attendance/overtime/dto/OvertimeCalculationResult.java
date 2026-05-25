package com.company.attendance.overtime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO returning the result of an overtime calculation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeCalculationResult {

    private BigDecimal effectiveOvertimeHours;
    private BigDecimal overtimeAmount;
    private BigDecimal weightedAverageRate;
    private boolean wasCapped;

    /**
     * Alias for getOvertimeAmount() to maintain compatibility with AttendanceServiceImpl.
     *
     * @return the total overtime amount
     */
    public BigDecimal getTotalAmount() {
        return overtimeAmount;
    }
}
