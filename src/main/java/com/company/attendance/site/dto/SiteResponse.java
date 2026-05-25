package com.company.attendance.site.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for a construction site.
 *
 * <p>Contains all site attributes including audit timestamps.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteResponse {

    private Long id;
    private String siteName;
    private String location;
    private Boolean activeStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
