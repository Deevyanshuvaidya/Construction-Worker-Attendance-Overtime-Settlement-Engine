package com.company.attendance.site.service;

import com.company.attendance.common.dto.PagedResponse;
import com.company.attendance.common.exception.ResourceNotFoundException;
import com.company.attendance.site.dto.*;
import com.company.attendance.site.entity.Site;
import com.company.attendance.site.mapper.SiteMapper;
import com.company.attendance.site.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link SiteService} providing site management operations.
 *
 * <p>Handles CRUD lifecycle, partial updates, soft-deletion, and paginated
 * search with optional filters.</p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private static final Logger log = LoggerFactory.getLogger(SiteServiceImpl.class);

    private final SiteRepository siteRepository;
    private final SiteMapper siteMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public SiteResponse createSite(SiteCreateRequest request) {
        log.info("Creating site with name: '{}'", request.getSiteName());

        Site site = siteMapper.toEntity(request);
        Site savedSite = siteRepository.save(site);

        log.info("Site created successfully with id: {}", savedSite.getId());
        return siteMapper.toResponse(savedSite);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the site is not found
     */
    @Override
    public SiteResponse updateSite(Long siteId, SiteUpdateRequest request) {
        log.info("Updating site with id: {}", siteId);

        Site site = findSiteOrThrow(siteId);

        // Apply non-null fields
        if (request.getSiteName() != null) {
            site.setSiteName(request.getSiteName());
        }
        if (request.getLocation() != null) {
            site.setLocation(request.getLocation());
        }

        Site updatedSite = siteRepository.save(site);

        log.info("Site updated successfully with id: {}", updatedSite.getId());
        return siteMapper.toResponse(updatedSite);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Performs a soft delete by setting {@code activeStatus} to {@code false}.</p>
     *
     * @throws ResourceNotFoundException if the site is not found
     */
    @Override
    public void deleteSite(Long siteId) {
        log.info("Soft-deleting site with id: {}", siteId);

        Site site = findSiteOrThrow(siteId);

        site.setActiveStatus(false);
        siteRepository.save(site);

        log.info("Site soft-deleted successfully with id: {}", siteId);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the site is not found
     */
    @Override
    @Transactional(readOnly = true)
    public SiteResponse getSiteById(Long siteId) {
        log.debug("Fetching site with id: {}", siteId);

        Site site = findSiteOrThrow(siteId);
        return siteMapper.toResponse(site);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SiteResponse> getAllSites(int page, int size, String sortBy, String sortDir,
                                                    String siteName, Boolean activeStatus) {
        log.debug("Fetching sites — page: {}, size: {}, sortBy: {}, sortDir: {}, siteName: {}, activeStatus: {}",
                page, size, sortBy, sortDir, siteName, activeStatus);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Site> sitePage = siteRepository.findWithFilters(siteName, activeStatus, pageable);

        List<SiteResponse> content = siteMapper.toResponseList(sitePage.getContent());

        return PagedResponse.<SiteResponse>builder()
                .content(content)
                .page(sitePage.getNumber())
                .size(sitePage.getSize())
                .totalElements(sitePage.getTotalElements())
                .totalPages(sitePage.getTotalPages())
                .last(sitePage.isLast())
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the site is not found
     */
    @Override
    public SiteResponse updateSiteStatus(Long siteId, SiteStatusUpdateRequest request) {
        log.info("Updating active status of site {} to {}", siteId, request.getActiveStatus());

        Site site = findSiteOrThrow(siteId);

        site.setActiveStatus(request.getActiveStatus());
        Site updatedSite = siteRepository.save(site);

        log.info("Site {} status updated to {}", siteId, request.getActiveStatus());
        return siteMapper.toResponse(updatedSite);
    }

    // ======================== Private helpers ========================

    /**
     * Finds a site by ID or throws {@link ResourceNotFoundException}.
     */
    private Site findSiteOrThrow(Long siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> {
                    log.warn("Site not found with id: {}", siteId);
                    return new ResourceNotFoundException("Site", "id", siteId);
                });
    }
}
