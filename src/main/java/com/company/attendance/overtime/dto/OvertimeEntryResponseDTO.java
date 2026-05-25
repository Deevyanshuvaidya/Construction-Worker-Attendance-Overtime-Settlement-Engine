package com.company.attendance.overtime.dto;

import com.company.attendance.overtime.entity.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO representing individual overtime entry details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeEntryResponseDTO {

    private Long id;
    private Long workerId;
    private LocalDate overtimeDate;
    private BigDecimal overtimeHours;
    private BigDecimal overtimeRateApplied;
    private BigDecimal overtimeAmount;
    private SettlementStatus settlementStatus;
}
