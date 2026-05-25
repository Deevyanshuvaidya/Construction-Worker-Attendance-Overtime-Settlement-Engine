package com.company.attendance.site.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for updating a site's active status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteStatusUpdateRequest {

    @NotNull(message = "Active status is required")
    private Boolean activeStatus;
}
