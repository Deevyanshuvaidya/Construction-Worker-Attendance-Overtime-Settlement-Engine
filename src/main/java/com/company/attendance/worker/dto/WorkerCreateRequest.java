package com.company.attendance.worker.dto;

import com.company.attendance.worker.entity.Designation;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new construction worker.
 *
 * <p>All mandatory fields are validated before the entity is persisted.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerCreateRequest {

    @NotBlank(message = "Worker name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String phone;

    @NotNull(message = "Designation is required")
    private Designation designation;

    @NotNull(message = "Daily wage rate is required")
    @Positive(message = "Daily wage rate must be positive")
    private BigDecimal dailyWageRate;

    private Boolean activeStatus = true;
}
