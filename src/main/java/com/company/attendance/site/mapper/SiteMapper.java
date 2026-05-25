package com.company.attendance.site.mapper;

import com.company.attendance.site.dto.SiteCreateRequest;
import com.company.attendance.site.dto.SiteResponse;
import com.company.attendance.site.entity.Site;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for converting between Site entities and DTOs.
 *
 * <p>Null source properties are ignored during mapping, enabling
 * safe partial-update workflows.</p>
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SiteMapper {

    /**
     * Converts a creation request DTO to a Site entity.
     *
     * @param request the creation request
     * @return the mapped Site entity
     */
    Site toEntity(SiteCreateRequest request);

    /**
     * Converts a Site entity to a response DTO.
     *
     * @param site the Site entity
     * @return the mapped response DTO
     */
    SiteResponse toResponse(Site site);

    /**
     * Converts a list of Site entities to a list of response DTOs.
     *
     * @param sites the list of Site entities
     * @return the mapped list of response DTOs
     */
    List<SiteResponse> toResponseList(List<Site> sites);
}
