package com.company.attendance.worker.dto;

import com.company.attendance.worker.entity.Designation;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for partially updating a construction worker.
 *
 * <p>All fields are optional. Only non-null fields will be applied to the entity.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerUpdateRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String phone;

    private Designation designation;

    @Positive(message = "Daily wage rate must be positive")
    private BigDecimal dailyWageRate;
}
