package com.company.attendance.worker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for updating a worker's active status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerStatusUpdateRequest {

    @NotNull(message = "Active status is required")
    private Boolean activeStatus;
}
