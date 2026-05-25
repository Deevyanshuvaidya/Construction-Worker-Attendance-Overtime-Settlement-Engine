package com.company.attendance.worker.entity;

import com.company.attendance.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a construction worker registered in the system.
 */
@Entity
@Table(name = "workers", indexes = {
        @Index(name = "idx_worker_phone", columnList = "phone", unique = true),
        @Index(name = "idx_worker_designation", columnList = "designation"),
        @Index(name = "idx_worker_active_status", columnList = "active_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Worker extends BaseEntity {

    @NotBlank(message = "Worker name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    @Column(name = "phone", nullable = false, unique = true, length = 15)
    private String phone;

    @NotNull(message = "Designation is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "designation", nullable = false, length = 20)
    private Designation designation;

    @NotNull(message = "Daily wage rate is required")
    @Positive(message = "Daily wage rate must be positive")
    @Column(name = "daily_wage_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyWageRate;

    @Column(name = "active_status", nullable = false)
    @Builder.Default
    private Boolean activeStatus = true;
}
