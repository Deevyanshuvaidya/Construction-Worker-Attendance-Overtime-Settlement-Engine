package com.company.attendance.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO for creating a new construction site.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteCreateRequest {

    @NotBlank(message = "Site name is required")
    @Size(min = 2, max = 200, message = "Site name must be between 2 and 200 characters")
    private String siteName;

    @NotBlank(message = "Location is required")
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    private Boolean activeStatus = true;
}
