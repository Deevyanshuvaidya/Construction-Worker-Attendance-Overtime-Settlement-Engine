package com.company.attendance.worker.service;

import com.company.attendance.common.exception.BusinessRuleViolationException;
import com.company.attendance.common.exception.DuplicateResourceException;
import com.company.attendance.common.exception.ResourceNotFoundException;
import com.company.attendance.worker.dto.*;
import com.company.attendance.worker.entity.Designation;
import com.company.attendance.worker.entity.Worker;
import com.company.attendance.worker.mapper.WorkerMapper;
import com.company.attendance.worker.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WorkerServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class WorkerServiceImplTest {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private WorkerMapper workerMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private WorkerServiceImpl workerService;

    private Worker worker;
    private WorkerCreateRequest workerCreateRequest;
    private WorkerResponse workerResponse;

    @BeforeEach
    void setUp() {
        worker = new Worker();
        worker.setId(1L);
        worker.setName("Ramesh Kumar");
        worker.setPhone("9876543210");
        worker.setDesignation(Designation.MASON);
        worker.setDailyWageRate(new BigDecimal("1200.00"));
        worker.setActiveStatus(true);

        workerCreateRequest = WorkerCreateRequest.builder()
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
    @DisplayName("Should create worker successfully")
    void createWorker_Success() {
        // Given
        when(workerRepository.existsByPhone(workerCreateRequest.getPhone())).thenReturn(false);
        when(workerMapper.toEntity(workerCreateRequest)).thenReturn(worker);
        when(workerRepository.save(any(Worker.class))).thenReturn(worker);
        when(workerMapper.toResponse(worker)).thenReturn(workerResponse);

        // When
        WorkerResponse result = workerService.createWorker(workerCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Ramesh Kumar", result.getName());
        assertEquals("9876543210", result.getPhone());
        assertEquals(Designation.MASON, result.getDesignation());
        verify(workerRepository).existsByPhone("9876543210");
        verify(workerRepository).save(any(Worker.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when phone already exists")
    void createWorker_DuplicatePhone_ThrowsDuplicateResourceException() {
        // Given
        when(workerRepository.existsByPhone(workerCreateRequest.getPhone())).thenReturn(true);

        // When & Then
        assertThrows(
                DuplicateResourceException.class,
                () -> workerService.createWorker(workerCreateRequest)
        );
        verify(workerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update worker successfully")
    void updateWorker_Success() {
        // Given
        WorkerUpdateRequest updateRequest = WorkerUpdateRequest.builder()
                .name("Ramesh Kumar Updated")
                .phone("9876543210")
                .designation(Designation.SUPERVISOR)
                .dailyWageRate(new BigDecimal("1500.00"))
                .build();

        Worker updatedWorker = new Worker();
        updatedWorker.setId(1L);
        updatedWorker.setName("Ramesh Kumar Updated");
        updatedWorker.setPhone("9876543210");
        updatedWorker.setDesignation(Designation.SUPERVISOR);
        updatedWorker.setDailyWageRate(new BigDecimal("1500.00"));
        updatedWorker.setActiveStatus(true);

        WorkerResponse updatedResponse = WorkerResponse.builder()
                .id(1L)
                .name("Ramesh Kumar Updated")
                .phone("9876543210")
                .designation(Designation.SUPERVISOR)
                .dailyWageRate(new BigDecimal("1500.00"))
                .activeStatus(true)
                .build();

        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(workerRepository.save(any(Worker.class))).thenReturn(updatedWorker);
        when(workerMapper.toResponse(any(Worker.class))).thenReturn(updatedResponse);

        // When
        WorkerResponse result = workerService.updateWorker(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Ramesh Kumar Updated", result.getName());
        assertEquals(Designation.SUPERVISOR, result.getDesignation());
        verify(workerRepository).findById(1L);
        verify(workerRepository).save(any(Worker.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent worker")
    void updateWorker_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(workerRepository.findById(99L)).thenReturn(Optional.empty());
        WorkerUpdateRequest updateRequest = WorkerUpdateRequest.builder()
                .name("Updated Name")
                .build();

        // When & Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> workerService.updateWorker(99L, updateRequest)
        );
        verify(workerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft-delete worker successfully when not clocked in")
    void deleteWorker_Success() {
        // Given
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(redisTemplate.hasKey("active_worker:1")).thenReturn(false);

        // When
        workerService.deleteWorker(1L);

        // Then
        verify(workerRepository).findById(1L);
        verify(redisTemplate).hasKey("active_worker:1");
        verify(workerRepository).save(any(Worker.class));
        assertFalse(worker.getActiveStatus());
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolationException when deleting clocked-in worker")
    void deleteWorker_ClockedIn_ThrowsBusinessRuleViolationException() {
        // Given
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(redisTemplate.hasKey("active_worker:1")).thenReturn(true);

        // When & Then
        assertThrows(
                BusinessRuleViolationException.class,
                () -> workerService.deleteWorker(1L)
        );
        verify(workerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return worker by ID successfully")
    void getWorkerById_Success() {
        // Given
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(workerMapper.toResponse(worker)).thenReturn(workerResponse);

        // When
        WorkerResponse result = workerService.getWorkerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ramesh Kumar", result.getName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when worker not found by ID")
    void getWorkerById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(workerRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> workerService.getWorkerById(99L)
        );
    }

    @Test
    @DisplayName("Should deactivate worker successfully when not clocked in")
    void updateWorkerStatus_Deactivate_Success() {
        // Given
        WorkerStatusUpdateRequest statusRequest = new WorkerStatusUpdateRequest(false);
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(redisTemplate.hasKey("active_worker:1")).thenReturn(false);
        when(workerRepository.save(any(Worker.class))).thenReturn(worker);
        
        WorkerResponse deactivatedResponse = WorkerResponse.builder()
                .id(1L)
                .name("Ramesh Kumar")
                .phone("9876543210")
                .designation(Designation.MASON)
                .dailyWageRate(new BigDecimal("1200.00"))
                .activeStatus(false)
                .build();
        when(workerMapper.toResponse(any(Worker.class))).thenReturn(deactivatedResponse);

        // When
        WorkerResponse result = workerService.updateWorkerStatus(1L, statusRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.getActiveStatus());
        verify(workerRepository).save(any(Worker.class));
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolationException when deactivating clocked-in worker")
    void updateWorkerStatus_Deactivate_ClockedIn_ThrowsBusinessRuleViolationException() {
        // Given
        WorkerStatusUpdateRequest statusRequest = new WorkerStatusUpdateRequest(false);
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(redisTemplate.hasKey("active_worker:1")).thenReturn(true);

        // When & Then
        assertThrows(
                BusinessRuleViolationException.class,
                () -> workerService.updateWorkerStatus(1L, statusRequest)
        );
        verify(workerRepository, never()).save(any());
    }
}
