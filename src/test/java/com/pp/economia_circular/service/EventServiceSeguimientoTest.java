package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.EventCreateDto;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Avisos a interesados y seguidores cuando se crean, cambian o cancelan eventos. */
@ExtendWith(MockitoExtension.class)
class EventServiceSeguimientoTest {

    @Mock private EventRepository eventRepository;
    @Mock private JWTService authService;
    @Mock private SeguimientoService seguimientoService;

    @InjectMocks
    private EventService eventService;

    private Usuario admin;
    private Event feria;
    private final LocalDateTime fecha = LocalDateTime.of(2026, 11, 20, 10, 0);

    @BeforeEach
    void setUp() {
        admin = new Usuario();
        admin.setId(1L);
        admin.setRol("ADMIN");
        feria = new Event();
        feria.setId(5L);
        feria.setEventName("Feria");
        feria.setEventDate(fecha);
        feria.setLocation("Plaza España");
        feria.setEventType(Event.EventType.FAIR);
        feria.setUsuario(admin);
        lenient().when(authService.getCurrentUser()).thenReturn(admin);
        lenient().when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private EventCreateDto dto(LocalDateTime fecha, String lugar) {
        EventCreateDto dto = new EventCreateDto();
        dto.setEventName("Feria");
        dto.setDescription("desc");
        dto.setEventDate(fecha);
        dto.setLocation(lugar);
        dto.setEventType(Event.EventType.FAIR);
        return dto;
    }

    @Test
    void describirCambiosDetectaFechaYLugar() {
        assertEquals("", EventService.describirCambios(feria, dto(fecha, "Plaza España")));
        assertEquals("Cambió la fecha.", EventService.describirCambios(feria, dto(fecha.plusDays(1), "Plaza España")));
        assertEquals("Cambió la fecha. Cambió el lugar.",
                EventService.describirCambios(feria, dto(fecha.plusDays(1), "Parque Sarmiento")));
    }

    @Test
    void crearEventoAvisaALosInteresados() {
        eventService.createEvent(dto(fecha, "Plaza España"));
        verify(seguimientoService).notificarEventoNuevo(any(Event.class));
    }

    @Test
    void cambiarLaFechaAvisaALosSeguidores() {
        when(eventRepository.findById(5L)).thenReturn(Optional.of(feria));

        eventService.updateEvent(5L, dto(fecha.plusDays(7), "Plaza España"));

        verify(seguimientoService).notificarEventoActualizado(eq(feria), eq("Cambió la fecha."));
    }

    @Test
    void editarSinCambiosRelevantesNoAvisa() {
        when(eventRepository.findById(5L)).thenReturn(Optional.of(feria));

        eventService.updateEvent(5L, dto(fecha, "Plaza España"));

        verify(seguimientoService, never()).notificarEventoActualizado(any(), anyString());
    }

    @Test
    void cancelarAvisaALosSeguidores() {
        when(eventRepository.findById(5L)).thenReturn(Optional.of(feria));

        eventService.deleteEvent(5L);

        assertEquals(Event.EventStatus.CANCELLED, feria.getStatus());
        verify(seguimientoService).notificarEventoActualizado(eq(feria), contains("cancelado"));
    }
}
