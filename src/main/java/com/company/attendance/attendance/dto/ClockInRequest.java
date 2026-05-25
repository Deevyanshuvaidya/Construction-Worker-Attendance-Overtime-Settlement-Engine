package com.company.attendance.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for worker clock-in operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClockInRequest {

    @NotNull(message = "Worker ID is required")
    private Long workerId;

    @NotNull(message = "Site ID is required")
    private Long siteId;
}
