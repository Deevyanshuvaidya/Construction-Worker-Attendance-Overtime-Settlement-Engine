package com.company.attendance.site.controller;

import com.company.attendance.common.dto.PagedResponse;
import com.company.attendance.site.dto.*;
import com.company.attendance.site.service.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing construction sites.
 *
 * <p>Exposes endpoints for CRUD operations, status toggling,
 * and paginated search with optional filters.</p>
 */
@RestController
@RequestMapping("/api/v1/sites")
@RequiredArgsConstructor
@Tag(name = "Site Management", description = "APIs for managing construction sites")
public class SiteController {

    private final SiteService siteService;

    /**
     * Creates a new site.
     *
     * @param request the site creation request
     * @return the created site with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create a new site", description = "Registers a new construction site in the system")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Site created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<SiteResponse> createSite(@Valid @RequestBody SiteCreateRequest request) {
        SiteResponse response = siteService.createSite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a site by ID.
     *
     * @param siteId the site ID
     * @return the site response with HTTP 200
     */
    @GetMapping("/{siteId}")
    @Operation(summary = "Get site by ID", description = "Retrieves details of a specific site")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Site retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<SiteResponse> getSiteById(
            @Parameter(description = "Site ID") @PathVariable Long siteId) {
        SiteResponse response = siteService.getSiteById(siteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a paginated, filterable list of sites.
     *
     * @param page         zero-based page index (default 0)
     * @param size         page size (default 20)
     * @param sortBy       sort field (default "id")
     * @param sortDir      sort direction (default "asc")
     * @param siteName     optional site-name filter
     * @param activeStatus optional active-status filter
     * @return a paged response of sites
     */
    @GetMapping
    @Operation(summary = "Get all sites", description = "Returns a paginated list of sites with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sites retrieved successfully")
    })
    public ResponseEntity<PagedResponse<SiteResponse>> getAllSites(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String sortDir,

            @Parameter(description = "Filter by site name (partial match)")
            @RequestParam(required = false) String siteName,

            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean activeStatus) {

        PagedResponse<SiteResponse> response = siteService.getAllSites(
                page, size, sortBy, sortDir, siteName, activeStatus);
        return ResponseEntity.ok(response);
    }

    /**
     * Partially updates a site.
     *
     * @param siteId  the site ID
     * @param request the update request
     * @return the updated site response with HTTP 200
     */
    @PutMapping("/{siteId}")
    @Operation(summary = "Update a site", description = "Partially updates an existing site's details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Site updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<SiteResponse> updateSite(
            @Parameter(description = "Site ID") @PathVariable Long siteId,
            @Valid @RequestBody SiteUpdateRequest request) {
        SiteResponse response = siteService.updateSite(siteId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft-deletes a site.
     *
     * @param siteId the site ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{siteId}")
    @Operation(summary = "Delete a site", description = "Soft-deletes a site by deactivating its status")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Site deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<Void> deleteSite(
            @Parameter(description = "Site ID") @PathVariable Long siteId) {
        siteService.deleteSite(siteId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the active status of a site.
     *
     * @param siteId  the site ID
     * @param request the status update request
     * @return the updated site response with HTTP 200
     */
    @PatchMapping("/{siteId}/status")
    @Operation(summary = "Update site status", description = "Activates or deactivates a site")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Site status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Site not found")
    })
    public ResponseEntity<SiteResponse> updateSiteStatus(
            @Parameter(description = "Site ID") @PathVariable Long siteId,
            @Valid @RequestBody SiteStatusUpdateRequest request) {
        SiteResponse response = siteService.updateSiteStatus(siteId, request);
        return ResponseEntity.ok(response);
    }
}
