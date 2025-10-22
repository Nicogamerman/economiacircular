package com.pp.economia_circular.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pp.economia_circular.DTO.EventCreateDto;
import com.pp.economia_circular.DTO.EventResponseDto;
import com.pp.economia_circular.entity.Event;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.ArticleService;
import com.pp.economia_circular.service.EventService;
import com.pp.economia_circular.service.JWTService;
import com.pp.economia_circular.service.ServicioMensaje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EventController.class)
@org.springframework.test.context.ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private ServicioMensaje servicioMensaje;

    @MockBean
    private MensajeRepository mensajeRepository;

    @MockBean
    private com.pp.economia_circular.service.RecyclingCenterService recyclingCenterService;

    @MockBean
    private com.pp.economia_circular.service.ReportService reportService;

    @MockBean
    private com.pp.economia_circular.service.EmailService emailService;

    @MockBean
    private com.pp.economia_circular.repositories.EventRepository eventRepository;

    @MockBean
    private com.pp.economia_circular.repositories.RecyclingCenterRepository recyclingCenterRepository;

    @MockBean
    private com.pp.economia_circular.repositories.TallerRepository tallerRepository;

    private EventCreateDto createDto;
    private EventResponseDto responseDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        createDto = new EventCreateDto();
        createDto.setEventName("Test Event");
        createDto.setDescription("Test Description");
        createDto.setEventDate(LocalDateTime.now().plusDays(7));
        createDto.setLocation("Test Location");
        createDto.setLatitude(40.7128);
        createDto.setLongitude(-74.0060);
        createDto.setEventType(Event.EventType.WORKSHOP);

        responseDto = new EventResponseDto();
        responseDto.setId(1L);
        responseDto.setEventName("Test Event");
        responseDto.setDescription("Test Description");
        responseDto.setEventDate(LocalDateTime.now().plusDays(7));
        responseDto.setLocation("Test Location");
        responseDto.setLatitude(40.7128);
        responseDto.setLongitude(-74.0060);
        responseDto.setEventType(Event.EventType.WORKSHOP);
        responseDto.setStatus(Event.EventStatus.ACTIVE);
        responseDto.setOrganizerId(1L);
        responseDto.setOrganizerName("admin@example.com");
        responseDto.setCreatedAt(LocalDateTime.now());
        responseDto.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEvent_AsAdmin_Success() throws Exception {
        // Arrange
        when(eventService.createEvent(any(EventCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.eventName").value("Test Event"))
                .andExpect(jsonPath("$.eventType").value("WORKSHOP"));

        verify(eventService, times(1)).createEvent(any(EventCreateDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createEvent_AsUser_Forbidden() throws Exception {
        // Act & Assert - usuario regular no puede crear eventos
        mockMvc.perform(post("/api/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());

        verify(eventService, never()).createEvent(any());
    }

    @Test
    void createEvent_Unauthorized() throws Exception {
        // Act & Assert - sin autenticación
        mockMvc.perform(post("/api/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());

        verify(eventService, never()).createEvent(any());
    }

    @Test
    void getAllEvents_Success() throws Exception {
        // Arrange
        when(eventService.getAllEvents()).thenReturn(Arrays.asList(responseDto));

        // Act & Assert
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventName").value("Test Event"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getUpcomingEvents_Success() throws Exception {
        // Arrange
        when(eventService.getUpcomingEvents()).thenReturn(Arrays.asList(responseDto));

        // Act & Assert
        mockMvc.perform(get("/api/events/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventName").value("Test Event"));
    }

    @Test
    void getEventsByType_Success() throws Exception {
        // Arrange
        when(eventService.getEventsByType(Event.EventType.WORKSHOP))
            .thenReturn(Arrays.asList(responseDto));

        // Act & Assert
        mockMvc.perform(get("/api/events/type/WORKSHOP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("WORKSHOP"));
    }

    @Test
    void getEventsNearLocation_Success() throws Exception {
        // Arrange
        when(eventService.getEventsNearLocation(anyDouble(), anyDouble(), anyDouble()))
            .thenReturn(Arrays.asList(responseDto));

        // Act & Assert
        mockMvc.perform(get("/api/events/nearby")
                .param("latitude", "40.7128")
                .param("longitude", "-74.0060")
                .param("radiusKm", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventName").value("Test Event"));
    }

    @Test
    void getEventsNearLocation_DefaultRadius() throws Exception {
        // Arrange
        when(eventService.getEventsNearLocation(anyDouble(), anyDouble(), eq(10.0)))
            .thenReturn(Arrays.asList(responseDto));

        // Act & Assert - sin especificar radiusKm, usa default 10
        mockMvc.perform(get("/api/events/nearby")
                .param("latitude", "40.7128")
                .param("longitude", "-74.0060"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEvent_AsAdmin_Success() throws Exception {
        // Arrange
        when(eventService.updateEvent(anyLong(), any(EventCreateDto.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/events/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(eventService, times(1)).updateEvent(anyLong(), any(EventCreateDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateEvent_AsUser_Forbidden() throws Exception {
        // Act & Assert - usuario regular no puede actualizar eventos
        mockMvc.perform(put("/api/events/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());

        verify(eventService, never()).updateEvent(anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEvent_NotFound() throws Exception {
        // Arrange
        when(eventService.updateEvent(anyLong(), any(EventCreateDto.class)))
            .thenThrow(new RuntimeException("Evento no encontrado"));

        // Act & Assert
        mockMvc.perform(put("/api/events/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Evento no encontrado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEvent_AsAdmin_Success() throws Exception {
        // Arrange
        doNothing().when(eventService).deleteEvent(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/events/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Evento eliminado exitosamente"));

        verify(eventService, times(1)).deleteEvent(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteEvent_AsUser_Forbidden() throws Exception {
        // Act & Assert - usuario regular no puede eliminar eventos
        mockMvc.perform(delete("/api/events/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(eventService, never()).deleteEvent(anyLong());
    }

    @Test
    void deleteEvent_Unauthorized() throws Exception {
        // Act & Assert - sin autenticación
        mockMvc.perform(delete("/api/events/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(eventService, never()).deleteEvent(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEvent_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Evento no encontrado"))
            .when(eventService).deleteEvent(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/events/999")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Evento no encontrado"));
    }
}

