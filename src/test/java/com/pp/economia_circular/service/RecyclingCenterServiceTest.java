package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.RecyclingCenterDto;
import com.pp.economia_circular.entity.RecyclingCenter;
import com.pp.economia_circular.repositories.RecyclingCenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecyclingCenterServiceTest {

    @Mock
    private RecyclingCenterRepository recyclingCenterRepository;

    @InjectMocks
    private RecyclingCenterService recyclingCenterService;

    private RecyclingCenter testCenter;
    private RecyclingCenterDto centerDto;

    @BeforeEach
    void setUp() {
        testCenter = new RecyclingCenter();
        testCenter.setId(1L);
        testCenter.setName("Test Recycling Center");
        testCenter.setDescription("Test Description");
        testCenter.setAddress("123 Test Street");
        testCenter.setLatitude(40.7128);
        testCenter.setLongitude(-74.0060);
        testCenter.setPhone("555-1234");
        testCenter.setEmail("test@recycling.com");
        testCenter.setCenterType(RecyclingCenter.CenterType.RECYCLING_CENTER);
        testCenter.setStatus(RecyclingCenter.CenterStatus.ACTIVE);
        testCenter.setOpeningHours("Mon-Fri 9AM-5PM");
        testCenter.setCreatedAt(LocalDateTime.now());
        testCenter.setUpdatedAt(LocalDateTime.now());

        centerDto = new RecyclingCenterDto();
        centerDto.setName("Test Recycling Center");
        centerDto.setDescription("Test Description");
        centerDto.setAddress("123 Test Street");
        centerDto.setLatitude(40.7128);
        centerDto.setLongitude(-74.0060);
        centerDto.setPhone("555-1234");
        centerDto.setEmail("test@recycling.com");
        centerDto.setCenterType(RecyclingCenter.CenterType.RECYCLING_CENTER);
        centerDto.setOpeningHours("Mon-Fri 9AM-5PM");
    }

    @Test
    void getById_Success() {
        // Arrange
        when(recyclingCenterRepository.findById(1L)).thenReturn(java.util.Optional.of(testCenter));

        // Act
        RecyclingCenter result = recyclingCenterService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testCenter.getId(), result.getId());
        assertEquals(testCenter.getName(), result.getName());
        verify(recyclingCenterRepository, times(1)).findById(1L);
    }

    @Test
    void getById_NotFound_ThrowsException() {
        // Arrange
        when(recyclingCenterRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> recyclingCenterService.getById(999L));
        assertEquals("Centro no encontrado", exception.getMessage());
        verify(recyclingCenterRepository, times(1)).findById(999L);
    }

    @Test
    void createCenter_Success() {
        // Arrange
        when(recyclingCenterRepository.save(any(RecyclingCenter.class))).thenReturn(testCenter);

        // Act
        RecyclingCenterDto result = recyclingCenterService.createCenter(centerDto);

        // Assert
        assertNotNull(result);
        assertEquals("Test Recycling Center", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("123 Test Street", result.getAddress());
        verify(recyclingCenterRepository, times(1)).save(any(RecyclingCenter.class));
    }

    @Test
    void getAllCenters_Success() {
        // Arrange
        doReturn(Arrays.asList(testCenter)).when(recyclingCenterRepository).findAllByStatus(RecyclingCenter.CenterStatus.ACTIVE);

        // Act
        List<RecyclingCenterDto> result = recyclingCenterService.getAllCenters();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Recycling Center", result.get(0).getName());
    }

    @Test
    void getCentersByType_Success() {
        // Arrange
        when(recyclingCenterRepository.findByCenterTypeAndStatus(
            RecyclingCenter.CenterType.RECYCLING_CENTER, 
            RecyclingCenter.CenterStatus.ACTIVE))
            .thenReturn(Arrays.asList(testCenter));

        // Act
        List<RecyclingCenterDto> result = recyclingCenterService.getCentersByType(
            RecyclingCenter.CenterType.RECYCLING_CENTER);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(RecyclingCenter.CenterType.RECYCLING_CENTER, result.get(0).getCenterType());
    }

    @Test
    void getCentersNearLocation_Success() {
        // Arrange
        doReturn(Arrays.asList(testCenter))
                .when(recyclingCenterRepository)
                .findAllByStatus(RecyclingCenter.CenterStatus.ACTIVE);
        doReturn(Arrays.asList(testCenter))
                .when(recyclingCenterRepository)
                .findAllByStatus(RecyclingCenter.CenterStatus.ACTIVE);

        // Act - búsqueda cerca de Nueva York
        List<RecyclingCenterDto> result = recyclingCenterService.getCentersNearLocation(
            40.7128, -74.0060, 10.0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getCentersNearLocation_NoResultsOutOfRange() {
        // Arrange
        when(recyclingCenterRepository.findAll()).thenReturn(Arrays.asList(testCenter));

        // Act - búsqueda muy lejos
        List<RecyclingCenterDto> result = recyclingCenterService.getCentersNearLocation(
            0.0, 0.0, 1.0);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void updateCenter_Success() {
        // Arrange
        when(recyclingCenterRepository.findById(1L)).thenReturn(Optional.of(testCenter));
        when(recyclingCenterRepository.save(any(RecyclingCenter.class))).thenReturn(testCenter);

        RecyclingCenterDto updateDto = new RecyclingCenterDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");
        updateDto.setAddress("456 New Street");
        updateDto.setLatitude(40.7589);
        updateDto.setLongitude(-73.9851);
        updateDto.setPhone("555-5678");
        updateDto.setEmail("updated@recycling.com");
        updateDto.setCenterType(RecyclingCenter.CenterType.WASTE_COLLECTION_POINT);
        updateDto.setOpeningHours("Mon-Sun 8AM-6PM");

        // Act
        RecyclingCenterDto result = recyclingCenterService.updateCenter(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(recyclingCenterRepository, times(1)).save(any(RecyclingCenter.class));
    }

    @Test
    void updateCenter_NotFound_ThrowsException() {
        // Arrange
        when(recyclingCenterRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> recyclingCenterService.updateCenter(999L, centerDto));
        assertEquals("Centro no encontrado", exception.getMessage());
    }

    @Test
    void deleteCenter_Success() {
        // Arrange
        when(recyclingCenterRepository.findById(1L)).thenReturn(Optional.of(testCenter));
        when(recyclingCenterRepository.save(any(RecyclingCenter.class))).thenReturn(testCenter);

        // Act
        recyclingCenterService.deleteCenter(1L);

        // Assert
        verify(recyclingCenterRepository, times(1)).save(any(RecyclingCenter.class));
        assertEquals(RecyclingCenter.CenterStatus.INACTIVE, testCenter.getStatus());
    }

    @Test
    void deleteCenter_NotFound_ThrowsException() {
        // Arrange
        when(recyclingCenterRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> recyclingCenterService.deleteCenter(999L));
        assertEquals("Centro no encontrado", exception.getMessage());
    }

    @Test
    void calculateDistance_SameLocation_ReturnsZero() {
        // Esta prueba verifica indirectamente el método privado calculateDistance
        
        // Arrange
        when(recyclingCenterRepository.findAll()).thenReturn(Arrays.asList(testCenter));

        // Act - misma ubicación exacta, radio muy pequeño
        List<RecyclingCenterDto> result = recyclingCenterService.getCentersNearLocation(
            testCenter.getLatitude(), 
            testCenter.getLongitude(), 
            0.1);

        // Assert - debe encontrar el centro en la misma ubicación
        assertEquals(1, result.size());
    }

    @Test
    void convertToDto_MapsAllFields() {
        // Arrange
        when(recyclingCenterRepository.findAllByStatus(RecyclingCenter.CenterStatus.ACTIVE)).thenReturn(Arrays.asList(testCenter));

        // Act
        List<RecyclingCenterDto> result = recyclingCenterService.getAllCenters();

        // Assert
        RecyclingCenterDto dto = result.get(0);
        assertEquals(testCenter.getId(), dto.getId());
        assertEquals(testCenter.getName(), dto.getName());
        assertEquals(testCenter.getDescription(), dto.getDescription());
        assertEquals(testCenter.getAddress(), dto.getAddress());
        assertEquals(testCenter.getLatitude(), dto.getLatitude());
        assertEquals(testCenter.getLongitude(), dto.getLongitude());
        assertEquals(testCenter.getPhone(), dto.getPhone());
        assertEquals(testCenter.getEmail(), dto.getEmail());
        assertEquals(testCenter.getCenterType(), dto.getCenterType());
        assertEquals(testCenter.getStatus(), dto.getStatus());
        assertEquals(testCenter.getOpeningHours(), dto.getOpeningHours());
    }
}

