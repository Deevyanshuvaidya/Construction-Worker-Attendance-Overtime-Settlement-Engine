package com.company.attendance.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for worker clock-out operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClockOutRequest {

    @NotNull(message = "Worker ID is required")
    private Long workerId;
}
