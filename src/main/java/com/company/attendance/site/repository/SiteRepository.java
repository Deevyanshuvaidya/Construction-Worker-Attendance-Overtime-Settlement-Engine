package com.company.attendance.site.repository;

import com.company.attendance.site.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Site} entities.
 *
 * <p>Provides basic CRUD, pagination, and custom filter queries for site management.</p>
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    /**
     * Returns a paginated list of sites whose name contains the given string (case-insensitive).
     *
     * @param siteName the partial name to search
     * @param pageable pagination parameters
     * @return a page of matching sites
     */
    Page<Site> findBySiteNameContainingIgnoreCase(String siteName, Pageable pageable);

    /**
     * Returns a paginated list of sites filtered by active status.
     *
     * @param activeStatus the status to filter by
     * @param pageable     pagination parameters
     * @return a page of matching sites
     */
    Page<Site> findByActiveStatus(Boolean activeStatus, Pageable pageable);

    /**
     * Returns a paginated list of sites matching optional filter criteria.
     *
     * <p>Any parameter that is {@code null} is ignored in the query.</p>
     *
     * @param siteName     partial name match (case-insensitive)
     * @param activeStatus exact active-status match
     * @param pageable     pagination parameters
     * @return a page of matching sites
     */
    @Query("SELECT s FROM Site s WHERE " +
           "(:siteName IS NULL OR LOWER(s.siteName) LIKE LOWER(CONCAT('%', :siteName, '%'))) AND " +
           "(:activeStatus IS NULL OR s.activeStatus = :activeStatus)")
    Page<Site> findWithFilters(
            @Param("siteName") String siteName,
            @Param("activeStatus") Boolean activeStatus,
            Pageable pageable);
}
