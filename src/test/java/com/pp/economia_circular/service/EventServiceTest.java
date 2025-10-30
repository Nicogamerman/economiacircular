package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.EventCreateDto;
import com.pp.economia_circular.DTO.EventResponseDto;
import com.pp.economia_circular.entity.Event;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.EventRepository;
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
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private JWTService authService;

    @InjectMocks
    private EventService eventService;

    private Usuario adminUser;
    private Usuario regularUser;
    private Event testEvent;
    private EventCreateDto createDto;

    @BeforeEach
    void setUp() {
        adminUser = new Usuario();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setNombre("Admin");
        adminUser.setRol("ADMIN");

        regularUser = new Usuario();
        regularUser.setId(2L);
        regularUser.setEmail("user@example.com");
        regularUser.setNombre("User");
        regularUser.setRol("USER");

        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setEventName("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setEventDate(LocalDateTime.now().plusDays(7));
        testEvent.setLocation("Test Location");
        testEvent.setLatitude(40.7128);
        testEvent.setLongitude(-74.0060);
        testEvent.setEventType(Event.EventType.WORKSHOP);
        testEvent.setStatus(Event.EventStatus.ACTIVE);
        testEvent.setUsuario(adminUser);
        testEvent.setCreatedAt(LocalDateTime.now());
        testEvent.setUpdatedAt(LocalDateTime.now());

        createDto = new EventCreateDto();
        createDto.setEventName("Test Event");
        createDto.setDescription("Test Description");
        createDto.setEventDate(LocalDateTime.now().plusDays(7));
        createDto.setLocation("Test Location");
        createDto.setLatitude(40.7128);
        createDto.setLongitude(-74.0060);
        createDto.setEventType(Event.EventType.WORKSHOP);
    }

    @Test
    void createEvent_AsAdmin_Success() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        EventResponseDto result = eventService.createEvent(createDto);

        // Assert
        assertNotNull(result);
        assertEquals("Test Event", result.getEventName());
        assertEquals("Test Description", result.getDescription());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createEvent_AsRegularUser_ThrowsException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(regularUser);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> eventService.createEvent(createDto));
        assertEquals("Solo los administradores pueden crear eventos", exception.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createEvent_NotAuthenticated_ThrowsException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> eventService.createEvent(createDto));
        assertEquals("Usuario no autenticado", exception.getMessage());
    }

    @Test
    void getAllEvents_Success() {
        // Arrange
        when(eventRepository.findByStatus(Event.EventStatus.ACTIVE))
            .thenReturn(Arrays.asList(testEvent));

        // Act
        List<EventResponseDto> result = eventService.getAllEvents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getEventName());
    }

    @Test
    void getUpcomingEvents_Success() {
        // Arrange
        when(eventRepository.findByEventDateAfterAndStatus(any(LocalDateTime.class), eq(Event.EventStatus.ACTIVE)))
            .thenReturn(Arrays.asList(testEvent));

        // Act
        List<EventResponseDto> result = eventService.getUpcomingEvents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getEventDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void getEventsByType_Success() {
        // Arrange
        when(eventRepository.findByEventTypeAndStatus(Event.EventType.WORKSHOP, Event.EventStatus.ACTIVE))
            .thenReturn(Arrays.asList(testEvent));

        // Act
        List<EventResponseDto> result = eventService.getEventsByType(Event.EventType.WORKSHOP);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Event.EventType.WORKSHOP, result.get(0).getEventType());
    }

    @Test
    void getEventsNearLocation_Success() {
        // Arrange
        when(eventRepository.findByStatus(Event.EventStatus.ACTIVE))
            .thenReturn(Arrays.asList(testEvent));

        // Act
        List<EventResponseDto> result = eventService.getEventsNearLocation(40.7128, -74.0060, 10.0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getEventsNearLocation_NoEventsInRange() {
        // Arrange
        when(eventRepository.findByStatus(Event.EventStatus.ACTIVE))
            .thenReturn(Arrays.asList(testEvent));

        // Act - búsqueda lejos de Nueva York
        List<EventResponseDto> result = eventService.getEventsNearLocation(0.0, 0.0, 1.0);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void updateEvent_AsOrganizer_Success() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        EventCreateDto updateDto = new EventCreateDto();
        updateDto.setEventName("Updated Event");
        updateDto.setDescription("Updated Description");
        updateDto.setEventDate(LocalDateTime.now().plusDays(14));
        updateDto.setLocation("Updated Location");
        updateDto.setLatitude(40.7589);
        updateDto.setLongitude(-73.9851);
        updateDto.setEventType(Event.EventType.EXCHANGE_EVENT);

        // Act
        EventResponseDto result = eventService.updateEvent(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void updateEvent_NotOrganizer_ThrowsException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(regularUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> eventService.updateEvent(1L, createDto));
        assertEquals("No tienes permisos para editar este evento", exception.getMessage());
    }

    @Test
    void updateEvent_NotFound_ThrowsException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> eventService.updateEvent(999L, createDto));
        assertEquals("Evento no encontrado", exception.getMessage());
    }

    @Test
    void deleteEvent_AsOrganizer_Success() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        eventService.deleteEvent(1L);

        // Assert
        verify(eventRepository, times(1)).save(any(Event.class));
        assertEquals(Event.EventStatus.CANCELLED, testEvent.getStatus());
    }

    @Test
    void deleteEvent_NotOrganizer_ThrowsException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(regularUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> eventService.deleteEvent(1L));
        assertEquals("No tienes permisos para eliminar este evento", exception.getMessage());
    }

    @Test
    void calculateDistance_SameLocation_ReturnsZero() {
        // Esta prueba verifica indirectamente el método privado calculateDistance
        // a través de getEventsNearLocation
        
        // Arrange
        when(eventRepository.findByStatus(Event.EventStatus.ACTIVE))
            .thenReturn(Arrays.asList(testEvent));

        // Act - misma ubicación exacta
        List<EventResponseDto> result = eventService.getEventsNearLocation(
            testEvent.getLatitude(), 
            testEvent.getLongitude(), 
            0.1);

        // Assert
        assertEquals(1, result.size());
    }
}

