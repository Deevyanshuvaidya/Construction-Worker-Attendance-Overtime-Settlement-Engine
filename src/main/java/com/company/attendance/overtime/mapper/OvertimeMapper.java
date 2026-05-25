package com.company.attendance.overtime.mapper;

import com.company.attendance.overtime.dto.OvertimeEntryResponseDTO;
import com.company.attendance.overtime.entity.OvertimeEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for converting between OvertimeEntry entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface OvertimeMapper {

    /**
     * Maps an OvertimeEntry entity to a response DTO.
     *
     * @param entry the OvertimeEntry entity
     * @return the mapped response DTO
     */
    @Mapping(target = "workerId", source = "worker.id")
    OvertimeEntryResponseDTO toResponseDTO(OvertimeEntry entry);

    /**
     * Maps a list of OvertimeEntry entities to a list of response DTOs.
     *
     * @param entries the list of OvertimeEntry entities
     * @return the mapped list of response DTOs
     */
    List<OvertimeEntryResponseDTO> toResponseDTOList(List<OvertimeEntry> entries);
}
