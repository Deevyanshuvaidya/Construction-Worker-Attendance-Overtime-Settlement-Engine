package com.company.attendance.site.service;

import com.company.attendance.common.dto.PagedResponse;
import com.company.attendance.site.dto.*;

/**
 * Service interface for site management operations.
 *
 * <p>Defines the contract for CRUD operations, status updates,
 * and paginated search with filters.</p>
 */
public interface SiteService {

    /**
     * Creates a new site.
     *
     * @param request the site creation request
     * @return the created site response
     */
    SiteResponse createSite(SiteCreateRequest request);

    /**
     * Partially updates an existing site.
     *
     * @param siteId  the ID of the site to update
     * @param request the update request containing non-null fields to apply
     * @return the updated site response
     */
    SiteResponse updateSite(Long siteId, SiteUpdateRequest request);

    /**
     * Soft-deletes a site by setting its status to inactive.
     *
     * @param siteId the ID of the site to delete
     */
    void deleteSite(Long siteId);

    /**
     * Retrieves a site by its ID.
     *
     * @param siteId the site ID
     * @return the site response
     */
    SiteResponse getSiteById(Long siteId);

    /**
     * Returns a paginated, filtered list of sites.
     *
     * @param page         zero-based page index
     * @param size         page size
     * @param sortBy       field to sort by
     * @param sortDir      sort direction ("asc" or "desc")
     * @param siteName     optional site-name filter (partial match)
     * @param activeStatus optional active-status filter
     * @return a paginated response of sites
     */
    PagedResponse<SiteResponse> getAllSites(int page, int size, String sortBy, String sortDir,
                                             String siteName, Boolean activeStatus);

    /**
     * Updates the active status of a site.
     *
     * @param siteId  the site ID
     * @param request the status update request
     * @return the updated site response
     */
    SiteResponse updateSiteStatus(Long siteId, SiteStatusUpdateRequest request);
}
