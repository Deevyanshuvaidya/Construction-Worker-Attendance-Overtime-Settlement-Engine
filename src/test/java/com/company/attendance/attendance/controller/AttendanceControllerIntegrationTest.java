package com.company.attendance.attendance.controller;

import com.company.attendance.attendance.dto.*;
import com.company.attendance.attendance.service.AttendanceService;
import com.company.attendance.common.dto.PagedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AttendanceController} using MockMvc.
 */
@WebMvcTest(AttendanceController.class)
class AttendanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AttendanceService attendanceService;

    private ClockInRequest validClockInRequest;
    private ClockOutRequest validClockOutRequest;
    private AttendanceLogResponse attendanceLogResponse;

    @BeforeEach
    void setUp() {
        validClockInRequest = new ClockInRequest(1L, 1L);
        validClockOutRequest = new ClockOutRequest(1L);

        attendanceLogResponse = AttendanceLogResponse.builder()
                .id(1L)
                .workerId(1L)
                .workerName("Ramesh Kumar")
                .siteId(1L)
                .siteName("Greenfield Towers")
                .clockInTimestamp(LocalDateTime.now())
                .flaggedForReview(false)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/attendance/clock-in - Should return 201 Created for valid clock-in")
    void clockIn_ReturnsCreated() throws Exception {
        // Given
        when(attendanceService.clockIn(any(ClockInRequest.class))).thenReturn(attendanceLogResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validClockInRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workerId").value(1))
                .andExpect(jsonPath("$.siteId").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/attendance/clock-in - Should return 400 Bad Request for invalid input")
    void clockIn_InvalidInput_ReturnsBadRequest() throws Exception {
        // Given — missing required workerId
        ClockInRequest invalidRequest = new ClockInRequest(null, 1L);

        // When & Then
        mockMvc.perform(post("/api/v1/attendance/clock-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/attendance/clock-out - Should return 200 OK for valid clock-out")
    void clockOut_ReturnsOk() throws Exception {
        // Given
        AttendanceLogResponse clockOutResponse = AttendanceLogResponse.builder()
                .id(1L)
                .workerId(1L)
                .workerName("Ramesh Kumar")
                .siteId(1L)
                .siteName("Greenfield Towers")
                .clockInTimestamp(LocalDateTime.now().minusHours(8))
                .clockOutTimestamp(LocalDateTime.now())
                .totalHoursWorked(new BigDecimal("8.00"))
                .overtimeHours(BigDecimal.ZERO)
                .flaggedForReview(false)
                .build();

        when(attendanceService.clockOut(any(ClockOutRequest.class))).thenReturn(clockOutResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/attendance/clock-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validClockOutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workerId").value(1))
                .andExpect(jsonPath("$.totalHoursWorked").value(8.00))
                .andExpect(jsonPath("$.flaggedForReview").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/active - Should return 200 OK with active worker list")
    void getActiveWorkers_ReturnsOk() throws Exception {
        // Given
        ActiveWorkerResponse activeWorker = ActiveWorkerResponse.builder()
                .workerId(1L)
                .workerName("Ramesh Kumar")
                .siteId(1L)
                .siteName("Greenfield Towers")
                .clockInTime(LocalDateTime.now())
                .build();

        when(attendanceService.getActiveWorkers()).thenReturn(List.of(activeWorker));

        // When & Then
        mockMvc.perform(get("/api/v1/attendance/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].workerId").value(1))
                .andExpect(jsonPath("$[0].workerName").value("Ramesh Kumar"));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/log - Should return 200 OK with paginated attendance logs")
    void getAttendanceLogs_ReturnsOk() throws Exception {
        // Given
        AttendanceLogResponse log1 = AttendanceLogResponse.builder()
                .id(1L)
                .workerId(1L)
                .workerName("Ramesh Kumar")
                .siteId(1L)
                .siteName("Greenfield Towers")
                .clockInTimestamp(LocalDateTime.of(2026, 5, 25, 8, 0))
                .clockOutTimestamp(LocalDateTime.of(2026, 5, 25, 16, 0))
                .totalHoursWorked(new BigDecimal("8.00"))
                .overtimeHours(BigDecimal.ZERO)
                .flaggedForReview(false)
                .build();

        PagedResponse<AttendanceLogResponse> pagedResponse = PagedResponse.<AttendanceLogResponse>builder()
                .content(List.of(log1))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(attendanceService.getAttendanceLogs(eq(1L), any(), any(), eq(0), eq(20), eq("createdAt"), eq("desc")))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/attendance/log")
                        .param("workerId", "1")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].totalHoursWorked").value(8.00));
    }
}
