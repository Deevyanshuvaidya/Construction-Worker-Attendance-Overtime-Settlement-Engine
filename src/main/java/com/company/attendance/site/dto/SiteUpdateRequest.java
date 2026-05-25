package com.company.attendance.site.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO for partially updating a construction site.
 *
 * <p>All fields are optional. Only non-null fields will be applied to the entity.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteUpdateRequest {

    @Size(min = 2, max = 200, message = "Site name must be between 2 and 200 characters")
    private String siteName;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;
}
