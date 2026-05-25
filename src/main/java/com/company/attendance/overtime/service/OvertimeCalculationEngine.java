package com.company.attendance.overtime.service;

import com.company.attendance.common.constants.AppConstants;
import com.company.attendance.overtime.dto.OvertimeCalculationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Core calculation engine for construction worker overtime payouts.
 * Uses a tiered multiplier system:
 * - First 2 hours of overtime at 1.5x hourly rate.
 * - Any subsequent hours at 2.0x hourly rate.
 * - Enforces a monthly overtime cap of 60 hours per worker.
 */
@Component
public class OvertimeCalculationEngine {

    /**
     * Inner class representing the specific OvertimeResult signature
     * expected by AttendanceServiceImpl.
     */
    public static class OvertimeResult extends OvertimeCalculationResult {
        public OvertimeResult(BigDecimal effectiveOvertimeHours, BigDecimal overtimeAmount, BigDecimal weightedAverageRate, boolean wasCapped) {
            super(effectiveOvertimeHours, overtimeAmount, weightedAverageRate, wasCapped);
        }
    }

    /**
     * Calculates the overtime details for a worker's shift.
     *
     * @param totalHoursWorked             total hours worked in the shift
     * @param dailyWageRate                worker's daily wage rate (for 8 standard hours)
     * @param existingMonthlyOvertimeHours overtime hours already accumulated by the worker this month
     * @return the calculated overtime results
     */
    public OvertimeResult calculate(BigDecimal totalHoursWorked, BigDecimal dailyWageRate, BigDecimal existingMonthlyOvertimeHours) {
        // 1. Calculate raw overtime hours (hours worked beyond the standard 8-hour shift)
        BigDecimal standardHours = AppConstants.STANDARD_SHIFT_HOURS;
        BigDecimal rawOvertime = totalHoursWorked.subtract(standardHours).max(BigDecimal.ZERO);

        // 2. Determine allowed overtime based on the monthly cap (60 hours)
        BigDecimal monthlyCap = AppConstants.MONTHLY_OVERTIME_CAP_HOURS;
        BigDecimal remainingCap = monthlyCap.subtract(existingMonthlyOvertimeHours).max(BigDecimal.ZERO);

        BigDecimal effectiveOvertime = rawOvertime.min(remainingCap);
        boolean wasCapped = rawOvertime.compareTo(remainingCap) > 0 || existingMonthlyOvertimeHours.compareTo(monthlyCap) >= 0;

        if (effectiveOvertime.compareTo(BigDecimal.ZERO) <= 0) {
            return new OvertimeResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, wasCapped);
        }

        // 3. Compute hourly rate (daily wage rate / 8 standard hours)
        BigDecimal hourlyRate = dailyWageRate.divide(standardHours, 2, RoundingMode.HALF_UP);

        // 4. Calculate tiered overtime (tier 1 is up to 2 hours at 1.5x, tier 2 is remainder at 2.0x)
        BigDecimal tier1Max = AppConstants.OVERTIME_TIER1_MAX_HOURS;
        BigDecimal tier1Hours = effectiveOvertime.min(tier1Max);
        BigDecimal tier2Hours = effectiveOvertime.subtract(tier1Hours).max(BigDecimal.ZERO);

        BigDecimal multiplierTier1 = AppConstants.OVERTIME_TIER1_RATE;
        BigDecimal multiplierTier2 = AppConstants.OVERTIME_TIER2_RATE;

        BigDecimal tier1Amount = tier1Hours.multiply(hourlyRate).multiply(multiplierTier1);
        BigDecimal tier2Amount = tier2Hours.multiply(hourlyRate).multiply(multiplierTier2);
        BigDecimal totalAmount = tier1Amount.add(tier2Amount).setScale(2, RoundingMode.HALF_UP);

        // 5. Calculate weighted average multiplier rate (e.g. 1.5 or 1.75 or 2.0)
        BigDecimal weightedMultiplierNumerator = tier1Hours.multiply(multiplierTier1).add(tier2Hours.multiply(multiplierTier2));
        BigDecimal weightedAverageMultiplier = weightedMultiplierNumerator.divide(effectiveOvertime, 2, RoundingMode.HALF_UP);

        return new OvertimeResult(effectiveOvertime, totalAmount, weightedAverageMultiplier, wasCapped);
    }
}
