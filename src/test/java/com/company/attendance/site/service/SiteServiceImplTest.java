package com.company.attendance.site.service;

import com.company.attendance.common.exception.ResourceNotFoundException;
import com.company.attendance.site.dto.*;
import com.company.attendance.site.entity.Site;
import com.company.attendance.site.mapper.SiteMapper;
import com.company.attendance.site.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SiteServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private SiteMapper siteMapper;

    @InjectMocks
    private SiteServiceImpl siteService;

    private Site site;
    private SiteCreateRequest siteCreateRequest;
    private SiteResponse siteResponse;

    @BeforeEach
    void setUp() {
        site = new Site();
        site.setId(1L);
        site.setSiteName("Greenfield Towers");
        site.setLocation("Whitefield, Bangalore");
        site.setActiveStatus(true);

        siteCreateRequest = SiteCreateRequest.builder()
                .siteName("Greenfield Towers")
                .location("Whitefield, Bangalore")
                .activeStatus(true)
                .build();

        siteResponse = SiteResponse.builder()
                .id(1L)
                .siteName("Greenfield Towers")
                .location("Whitefield, Bangalore")
                .activeStatus(true)
                .build();
    }

    @Test
    @DisplayName("Should create site successfully")
    void createSite_Success() {
        // Given
        when(siteMapper.toEntity(any(SiteCreateRequest.class))).thenReturn(site);
        when(siteRepository.save(any(Site.class))).thenReturn(site);
        when(siteMapper.toResponse(site)).thenReturn(siteResponse);

        // When
        SiteResponse result = siteService.createSite(siteCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Greenfield Towers", result.getSiteName());
        assertEquals("Whitefield, Bangalore", result.getLocation());
        assertTrue(result.getActiveStatus());
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Should update site successfully")
    void updateSite_Success() {
        // Given
        SiteUpdateRequest updateRequest = SiteUpdateRequest.builder()
                .siteName("Greenfield Towers Phase 2")
                .location("Whitefield, Bangalore")
                .build();

        Site updatedSite = new Site();
        updatedSite.setId(1L);
        updatedSite.setSiteName("Greenfield Towers Phase 2");
        updatedSite.setLocation("Whitefield, Bangalore");
        updatedSite.setActiveStatus(true);

        SiteResponse updatedResponse = SiteResponse.builder()
                .id(1L)
                .siteName("Greenfield Towers Phase 2")
                .location("Whitefield, Bangalore")
                .activeStatus(true)
                .build();

        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(siteRepository.save(any(Site.class))).thenReturn(updatedSite);
        when(siteMapper.toResponse(any(Site.class))).thenReturn(updatedResponse);

        // When
        SiteResponse result = siteService.updateSite(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Greenfield Towers Phase 2", result.getSiteName());
        verify(siteRepository).findById(1L);
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent site")
    void updateSite_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(siteRepository.findById(99L)).thenReturn(Optional.empty());
        SiteUpdateRequest updateRequest = SiteUpdateRequest.builder()
                .siteName("Updated Site")
                .build();

        // When & Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> siteService.updateSite(99L, updateRequest)
        );
        verify(siteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete site successfully")
    void deleteSite_Success() {
        // Given
        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));

        // When
        siteService.deleteSite(1L);

        // Then
        verify(siteRepository).findById(1L);
        verify(siteRepository).save(any(Site.class));
        assertFalse(site.getActiveStatus());
    }

    @Test
    @DisplayName("Should return site by ID successfully")
    void getSiteById_Success() {
        // Given
        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(siteMapper.toResponse(site)).thenReturn(siteResponse);

        // When
        SiteResponse result = siteService.getSiteById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Greenfield Towers", result.getSiteName());
        verify(siteRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when site not found by ID")
    void getSiteById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(siteRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> siteService.getSiteById(99L)
        );
    }

    @Test
    @DisplayName("Should update site status successfully")
    void updateSiteStatus_Success() {
        // Given
        SiteStatusUpdateRequest statusRequest = new SiteStatusUpdateRequest(false);
        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(siteRepository.save(any(Site.class))).thenReturn(site);
        
        SiteResponse deactivatedResponse = SiteResponse.builder()
                .id(1L)
                .siteName("Greenfield Towers")
                .location("Whitefield, Bangalore")
                .activeStatus(false)
                .build();
        when(siteMapper.toResponse(any(Site.class))).thenReturn(deactivatedResponse);

        // When
        SiteResponse result = siteService.updateSiteStatus(1L, statusRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.getActiveStatus());
        verify(siteRepository).findById(1L);
        verify(siteRepository).save(any(Site.class));
    }
}
