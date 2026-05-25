package com.company.attendance.overtime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing monthly overtime accumulation and payouts for a worker.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeSummaryDTO {

    private Long workerId;
    private String workerName;
    private int year;
    private int month;
    private BigDecimal totalOvertimeHours;
    private BigDecimal totalOvertimeAmount;
    private List<OvertimeEntryResponseDTO> entries;
    private boolean wasCapped;
}
