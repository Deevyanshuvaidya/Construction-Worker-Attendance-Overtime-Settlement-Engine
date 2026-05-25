package com.company.attendance.worker.dto;

import com.company.attendance.worker.entity.Designation;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a construction worker.
 *
 * <p>Contains all worker attributes including audit timestamps.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerResponse {

    private Long id;
    private String name;
    private String phone;
    private Designation designation;
    private BigDecimal dailyWageRate;
    private Boolean activeStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
