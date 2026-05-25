package com.company.attendance.worker.mapper;

import com.company.attendance.worker.dto.WorkerCreateRequest;
import com.company.attendance.worker.dto.WorkerResponse;
import com.company.attendance.worker.entity.Worker;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for converting between Worker entities and DTOs.
 *
 * <p>Null source properties are ignored during mapping, enabling
 * safe partial-update workflows.</p>
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WorkerMapper {

    /**
     * Converts a creation request DTO to a Worker entity.
     *
     * @param request the creation request
     * @return the mapped Worker entity
     */
    Worker toEntity(WorkerCreateRequest request);

    /**
     * Converts a Worker entity to a response DTO.
     *
     * @param worker the Worker entity
     * @return the mapped response DTO
     */
    WorkerResponse toResponse(Worker worker);

    /**
     * Converts a list of Worker entities to a list of response DTOs.
     *
     * @param workers the list of Worker entities
     * @return the mapped list of response DTOs
     */
    List<WorkerResponse> toResponseList(List<Worker> workers);
}
