package com.pp.economia_circular.service;


import com.pp.economia_circular.DTO.EventCreateDto;
import com.pp.economia_circular.DTO.EventResponseDto;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.Event;
import com.pp.economia_circular.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private JWTService authService;
    
    public EventResponseDto createEvent(EventCreateDto createDto) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        // Solo administradores pueden crear eventos
        if (!"ADMIN".equals(currentUser.getRol())) {
            throw new RuntimeException("Solo los administradores pueden crear eventos");
        }
        
        Event event = new Event();
        event.setEventName(createDto.getEventName());
        event.setDescription(createDto.getDescription());
        event.setEventDate(createDto.getEventDate());
        event.setLocation(createDto.getLocation());
        event.setLatitude(createDto.getLatitude());
        event.setLongitude(createDto.getLongitude());
        event.setEventType(createDto.getEventType());
        event.setUsuario(currentUser);
        
        Event savedEvent = eventRepository.save(event);
        return convertToResponseDto(savedEvent);
    }
    
    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findByStatus(Event.EventStatus.ACTIVE).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public List<EventResponseDto> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByEventDateAfterAndStatus(now, Event.EventStatus.ACTIVE).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public List<EventResponseDto> getEventsByType(Event.EventType eventType) {
        return eventRepository.findByEventTypeAndStatus(eventType, Event.EventStatus.ACTIVE).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public Event getEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }

    public List<EventResponseDto> getEventsNearLocation(Double latitude, Double longitude, Double radiusKm) {
        // Implementación simplificada - en producción usarías una consulta espacial
        return eventRepository.findByStatus(Event.EventStatus.ACTIVE).stream()
                .filter(event -> event.getLatitude() != null && event.getLongitude() != null)
                .filter(event -> calculateDistance(latitude, longitude, 
                        event.getLatitude(), event.getLongitude()) <= radiusKm)
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public EventResponseDto updateEvent(Long id, EventCreateDto updateDto) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        // Solo el organizador o administradores pueden editar eventos
        if (!event.getUsuario().getId().equals(currentUser.getId()) &&
            !"ADMIN".equals(currentUser.getRol())) {
            throw new RuntimeException("No tienes permisos para editar este evento");
        }
        
        event.setEventName(updateDto.getEventName());
        event.setDescription(updateDto.getDescription());
        event.setEventDate(updateDto.getEventDate());
        event.setLocation(updateDto.getLocation());
        event.setLatitude(updateDto.getLatitude());
        event.setLongitude(updateDto.getLongitude());
        event.setEventType(updateDto.getEventType());
        
        Event updatedEvent = eventRepository.save(event);
        return convertToResponseDto(updatedEvent);
    }
    
    public void deleteEvent(Long id) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        // Solo el organizador o administradores pueden eliminar eventos
        if (!event.getUsuario().getId().equals(currentUser.getId()) &&
            !"ADMIN".equals(currentUser.getRol())) {
            throw new RuntimeException("No tienes permisos para eliminar este evento");
        }
        
        event.setStatus(Event.EventStatus.CANCELLED);
        eventRepository.save(event);
    }
    
    private EventResponseDto convertToResponseDto(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setEventName(event.getEventName());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setLocation(event.getLocation());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setEventType(event.getEventType());
        dto.setStatus(event.getStatus());
        dto.setOrganizerId(event.getUsuario().getId());
        dto.setOrganizerName(event.getUsuario().getEmail());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        return dto;
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en kilómetros
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
