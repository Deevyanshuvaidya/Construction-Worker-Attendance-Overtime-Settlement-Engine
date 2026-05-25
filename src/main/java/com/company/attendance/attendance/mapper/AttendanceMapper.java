package com.company.attendance.attendance.mapper;

import com.company.attendance.attendance.dto.AttendanceLogResponse;
import com.company.attendance.attendance.entity.AttendanceLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for converting {@link AttendanceLog} entities to {@link AttendanceLogResponse} DTOs.
 * Flattens nested Worker and Site associations into top-level response fields.
 */
@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(source = "worker.id", target = "workerId")
    @Mapping(source = "worker.name", target = "workerName")
    @Mapping(source = "site.id", target = "siteId")
    @Mapping(source = "site.siteName", target = "siteName")
    AttendanceLogResponse toResponse(AttendanceLog attendanceLog);

    List<AttendanceLogResponse> toResponseList(List<AttendanceLog> logs);
}
