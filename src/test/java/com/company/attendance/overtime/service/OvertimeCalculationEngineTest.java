package com.company.attendance.overtime.service;

import com.company.attendance.overtime.dto.OvertimeCalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OvertimeCalculationEngine}.
 * <p>
 * The engine is pure business logic with no Spring dependencies.
 * Constants used:
 *   STANDARD_SHIFT_HOURS = 8
 *   OVERTIME_TIER1_MAX_HOURS = 2
 *   OVERTIME_TIER1_RATE = 1.5
 *   OVERTIME_TIER2_RATE = 2.0
 *   MONTHLY_OVERTIME_CAP_HOURS = 60
 *   MAX_SHIFT_HOURS = 16
 * <p>
 * Hourly rate = dailyWageRate / STANDARD_SHIFT_HOURS
 * For dailyRate 1200 → hourlyRate = 150
 */
class OvertimeCalculationEngineTest {

    private OvertimeCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new OvertimeCalculationEngine();
    }

    @Test
    @DisplayName("Should return zero overtime for standard 8-hour shift")
    void shouldReturnZeroOvertimeForStandard8HourShift() {
        // Given
        BigDecimal totalHours = new BigDecimal("8");
        BigDecimal dailyRate = new BigDecimal("1200");
        BigDecimal existingOvertimeThisMonth = BigDecimal.ZERO;

        // When
        OvertimeCalculationResult result = engine.calculate(totalHours, dailyRate, existingOvertimeThisMonth);

        // Then
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getEffectiveOvertimeHours()),
                "Effective overtime hours should be 0 for an 8-hour shift");
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getOvertimeAmount()),
                "Overtime amount should be 0 for an 8-hour shift");
        assertFalse(result.isWasCapped(), "Should not be capped for standard shift");
    }

    @Test
    @DisplayName("Should calculate tier 1 overtime for 10-hour shift")
    void shouldCalculateTier1OvertimeFor10HourShift() {
        // Given
        BigDecimal totalHours = new BigDecimal("10");
        BigDecimal dailyRate = new BigDecimal("1200");
        BigDecimal existingOvertimeThisMonth = BigDecimal.ZERO;

        // When
        OvertimeCalculationResult result = engine.calculate(totalHours, dailyRate, existingOvertimeThisMonth);

        // Then
        // 10 - 8 = 2 hours overtime, all in tier 1
        BigDecimal expectedOT = new BigDecimal("2");
        assertEquals(0, expectedOT.compareTo(result.getEffectiveOvertimeHours()),
                "Effective overtime hours should be 2");

        // hourlyRate = 1200 / 8 = 150
        // tier1Amount = 2 * 150 * 1.5 = 450
        BigDecimal expectedAmount = new BigDecimal("450.00");
        assertEquals(0, expectedAmount.compareTo(result.getOvertimeAmount()),
                "Overtime amount should be 450.00 (2h × 150 × 1.5)");
        assertFalse(result.isWasCapped(), "Should not be capped");
    }

    @Test
    @DisplayName("Should calculate both tier 1 and tier 2 overtime for 12-hour shift")
    void shouldCalculateBothTier1AndTier2OvertimeFor12HourShift() {
        // Given
        BigDecimal totalHours = new BigDecimal("12");
        BigDecimal dailyRate = new BigDecimal("1200");
        BigDecimal existingOvertimeThisMonth = BigDecimal.ZERO;

        // When
        OvertimeCalculationResult result = engine.calculate(totalHours, dailyRate, existingOvertimeThisMonth);

        // Then
        // 12 - 8 = 4 hours overtime
        BigDecimal expectedOT = new BigDecimal("4");
        assertEquals(0, expectedOT.compareTo(result.getEffectiveOvertimeHours()),
                "Effective overtime hours should be 4");

        // hourlyRate = 1200 / 8 = 150
        // tier1: 2h × 150 × 1.5 = 450
        // tier2: 2h × 150 × 2.0 = 600
        // total = 1050
        BigDecimal expectedAmount = new BigDecimal("1050.00");
        assertEquals(0, expectedAmount.compareTo(result.getOvertimeAmount()),
                "Overtime amount should be 1050.00 (tier1=450 + tier2=600)");
        assertFalse(result.isWasCapped(), "Should not be capped");
    }

    @Test
    @DisplayName("Should cap overtime at monthly limit")
    void shouldCapOvertimeAtMonthlyLimit() {
        // Given
        BigDecimal totalHours = new BigDecimal("14");
        BigDecimal dailyRate = new BigDecimal("1200");
        BigDecimal existingOvertimeThisMonth = new BigDecimal("58");

        // When
        OvertimeCalculationResult result = engine.calculate(totalHours, dailyRate, existingOvertimeThisMonth);

        // Then
        // Raw OT = 14 - 8 = 6, but monthly cap = 60, existing = 58, so only 2 allowed
        BigDecimal expectedOT = new BigDecimal("2");
        assertEquals(0, expectedOT.compareTo(result.getEffectiveOvertimeHours()),
                "Effective overtime hours should be capped to 2");
        assertTrue(result.isWasCapped(), "Should be flagged as capped");

        // 2h of tier1 only: 2 × 150 × 1.5 = 450
        BigDecimal expectedAmount = new BigDecimal("450.00");
        assertEquals(0, expectedAmount.compareTo(result.getOvertimeAmount()),
                "Overtime amount should be 450.00 for the 2 allowed hours");
    }

    @Test
    @DisplayName("Should return zero when monthly cap already reached")
    void shouldReturnZeroWhenMonthlyCaAlreadyReached() {
        // Given
        BigDecimal totalHours = new BigDecimal("12");
        BigDecimal dailyRate = new BigDecimal("1200");
        BigDecimal existingOvertimeThisMonth = new BigDecimal("60");

        // When
        OvertimeCalculationResult result = engine.calculate(totalHours, dailyRate, existingOvertimeThisMonth);

        // Then
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getEffectiveOvertimeHours()),
                "Effective overtime should be 0 when monthly cap already reached");
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getOvertimeAmount()),
                "Amount should be 0 when monthly cap already reached");
        assertTrue(result.isWasCapped(), "Should be flagged as capped");
    }

    @Test
    @DisplayName("Should return zero overtime for less than 8 hours")
    void shouldReturnZeroOvertimeForLessThan8Hours() {
        // Given
        BigDecimal totalHours = new BigDecimal("6");
        BigDecimal dailyRate = new BigDecimal("1200");
        BigDecimal existingOvertimeThisMonth = BigDecimal.ZERO;

        // When
        OvertimeCalculationResult result = engine.calculate(totalHours, dailyRate, existingOvertimeThisMonth);

        // Then
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getEffectiveOvertimeHours()),
                "Effective overtime should be 0 for 6-hour shift");
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getOvertimeAmount()),
                "Amount should be 0 for 6-hour shift");
        assertFalse(result.isWasCapped(), "Should not be capped");
    }

    @Test
    @DisplayName("Should handle exactly 8 hours with no overtime")
    void shouldHandleExactly8HoursWithNoOvertime() {
        // Given
        BigDecimal totalHours = new BigDecimal("8.00");
        BigDecimal dailyRate = new BigDecimal("1200");
        BigDecimal existingOvertimeThisMonth = BigDecimal.ZERO;

        // When
        OvertimeCalculationResult result = engine.calculate(totalHours, dailyRate, existingOvertimeThisMonth);

        // Then
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getEffectiveOvertimeHours()),
                "Effective overtime should be 0 for exactly 8.00 hours");
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getOvertimeAmount()),
                "Amount should be 0 for exactly 8.00 hours");
        assertFalse(result.isWasCapped(), "Should not be capped for exactly 8 hours");
    }
}
