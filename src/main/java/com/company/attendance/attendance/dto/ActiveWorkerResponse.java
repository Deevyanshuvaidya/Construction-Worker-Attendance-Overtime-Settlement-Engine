package com.company.attendance.attendance.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Response DTO representing a currently active (clocked-in) worker.
 * Implements {@link Serializable} for Redis storage compatibility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveWorkerResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long workerId;
    private String workerName;
    private Long siteId;
    private String siteName;
    private LocalDateTime clockInTime;
}
