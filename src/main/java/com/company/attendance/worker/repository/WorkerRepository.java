package com.company.attendance.worker.repository;

import com.company.attendance.worker.entity.Designation;
import com.company.attendance.worker.entity.Worker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Worker} entities.
 *
 * <p>Provides basic CRUD, pagination, specification-based queries,
 * and custom filter queries for worker management.</p>
 */
@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long>, JpaSpecificationExecutor<Worker> {

    /**
     * Finds a worker by phone number.
     *
     * @param phone the phone number to search
     * @return an {@link Optional} containing the worker if found
     */
    Optional<Worker> findByPhone(String phone);

    /**
     * Checks whether a worker with the given phone number exists.
     *
     * @param phone the phone number to check
     * @return {@code true} if a worker with the phone already exists
     */
    boolean existsByPhone(String phone);

    /**
     * Returns a paginated list of workers filtered by active status.
     *
     * @param activeStatus the status to filter by
     * @param pageable     pagination parameters
     * @return a page of matching workers
     */
    Page<Worker> findByActiveStatus(Boolean activeStatus, Pageable pageable);

    /**
     * Returns a paginated list of workers matching optional filter criteria.
     *
     * <p>Any parameter that is {@code null} is ignored in the query.</p>
     *
     * @param name         partial name match (case-insensitive)
     * @param designation  exact designation match
     * @param activeStatus exact active-status match
     * @param pageable     pagination parameters
     * @return a page of matching workers
     */
    @Query("SELECT w FROM Worker w WHERE " +
           "(:name IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:designation IS NULL OR w.designation = :designation) AND " +
           "(:activeStatus IS NULL OR w.activeStatus = :activeStatus)")
    Page<Worker> findWithFilters(
            @Param("name") String name,
            @Param("designation") Designation designation,
            @Param("activeStatus") Boolean activeStatus,
            Pageable pageable);
}
