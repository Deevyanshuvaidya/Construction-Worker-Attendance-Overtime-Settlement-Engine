package com.company.attendance.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper used by all list endpoints.
 *
 * <p>Wraps a page of results with pagination metadata so that API consumers
 * can implement cursor-less pagination consistently across every resource.</p>
 *
 * @param <T> the type of element contained in this page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    /** The slice of results for the current page. */
    private List<T> content;

    /** Zero-based page index. */
    private int page;

    /** Requested page size. */
    private int size;

    /** Total number of elements across all pages. */
    private long totalElements;

    /** Total number of pages. */
    private int totalPages;

    /** {@code true} if this is the last page. */
    private boolean last;
}
