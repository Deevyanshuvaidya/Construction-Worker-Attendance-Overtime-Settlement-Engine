package com.company.attendance.site.entity;

import com.company.attendance.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * JPA entity representing a construction site.
 *
 * <p>Each site has a unique name, a human-readable location description,
 * and an active/inactive status flag.</p>
 */
@Entity
@Table(name = "sites", indexes = {
        @Index(name = "idx_site_name", columnList = "site_name"),
        @Index(name = "idx_site_active_status", columnList = "active_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site extends BaseEntity {

    @NotBlank(message = "Site name is required")
    @Size(max = 200, message = "Site name must not exceed 200 characters")
    @Column(name = "site_name", nullable = false, length = 200)
    private String siteName;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "active_status", nullable = false)
    @Builder.Default
    private Boolean activeStatus = true;
}
