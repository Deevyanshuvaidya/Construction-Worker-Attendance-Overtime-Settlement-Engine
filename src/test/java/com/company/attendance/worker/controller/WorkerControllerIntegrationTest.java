package com.company.attendance.worker.controller;

import com.company.attendance.common.dto.PagedResponse;
import com.company.attendance.common.exception.ResourceNotFoundException;
import com.company.attendance.worker.dto.*;
import com.company.attendance.worker.entity.Designation;
import com.company.attendance.worker.service.WorkerService;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link WorkerController} using MockMvc.
 */
@WebMvcTest(WorkerController.class)
class WorkerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkerService workerService;

    private WorkerCreateRequest validRequest;
    private WorkerResponse workerResponse;

    @BeforeEach
    void setUp() {
        validRequest = WorkerCreateRequest.builder()
                .name("Ramesh Kumar")
                .phone("9876543210")
                .designation(Designation.MASON)
                .dailyWageRate(new BigDecimal("1200.00"))
                .activeStatus(true)
                .build();

        workerResponse = WorkerResponse.builder()
                .id(1L)
                .name("Ramesh Kumar")
                .phone("9876543210")
                .designation(Designation.MASON)
                .dailyWageRate(new BigDecimal("1200.00"))
                .activeStatus(true)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/workers - Should return 201 Created for valid input")
    void createWorker_ReturnsCreated() throws Exception {
        // Given
        when(workerService.createWorker(any(WorkerCreateRequest.class))).thenReturn(workerResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ramesh Kumar"))
                .andExpect(jsonPath("$.phone").value("9876543210"))
                .andExpect(jsonPath("$.designation").value("MASON"))
                .andExpect(jsonPath("$.activeStatus").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/workers - Should return 400 Bad Request for invalid input")
    void createWorker_InvalidInput_ReturnsBadRequest() throws Exception {
        // Given — empty name
        WorkerCreateRequest invalidRequest = WorkerCreateRequest.builder()
                .name("")
                .phone("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/workers/{id} - Should return 200 OK with worker data")
    void getWorkerById_ReturnsOk() throws Exception {
        // Given
        when(workerService.getWorkerById(1L)).thenReturn(workerResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/workers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ramesh Kumar"))
                .andExpect(jsonPath("$.phone").value("9876543210"));
    }

    @Test
    @DisplayName("GET /api/v1/workers/{id} - Should return 404 Not Found when worker does not exist")
    void getWorkerById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(workerService.getWorkerById(99L))
                .thenThrow(new ResourceNotFoundException("Worker", "id", 99L));

        // When & Then
        mockMvc.perform(get("/api/v1/workers/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/workers - Should return 200 OK with page of workers")
    void getAllWorkers_ReturnsOk() throws Exception {
        // Given
        WorkerResponse worker2 = WorkerResponse.builder()
                .id(2L)
                .name("Suresh Yadav")
                .phone("9876543211")
                .designation(Designation.ELECTRICIAN)
                .dailyWageRate(new BigDecimal("1500.00"))
                .activeStatus(true)
                .build();

        PagedResponse<WorkerResponse> pagedResponse = PagedResponse.<WorkerResponse>builder()
                .content(List.of(workerResponse, worker2))
                .page(0)
                .size(20)
                .totalElements(2)
                .totalPages(1)
                .last(true)
                .build();

        when(workerService.getAllWorkers(0, 20, "id", "asc", null, null, null))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/workers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("Ramesh Kumar"))
                .andExpect(jsonPath("$.content[1].name").value("Suresh Yadav"));
    }

    @Test
    @DisplayName("PATCH /api/v1/workers/{id}/status - Should return 200 OK on status update")
    void updateWorkerStatus_ReturnsOk() throws Exception {
        // Given
        WorkerStatusUpdateRequest statusUpdateRequest = new WorkerStatusUpdateRequest(false);
        WorkerResponse deactivatedResponse = WorkerResponse.builder()
                .id(1L)
                .name("Ramesh Kumar")
                .phone("9876543210")
                .designation(Designation.MASON)
                .dailyWageRate(new BigDecimal("1200.00"))
                .activeStatus(false)
                .build();

        when(workerService.updateWorkerStatus(eq(1L), any(WorkerStatusUpdateRequest.class)))
                .thenReturn(deactivatedResponse);

        // When & Then
        mockMvc.perform(patch("/api/v1/workers/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeStatus").value(false));
    }
}
