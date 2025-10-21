package com.pp.economia_circular.DTO;


import com.pp.economia_circular.entity.Event;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class EventCreateDto {
    
    @NotBlank(message = "El nombre del evento es obligatorio")
    private String eventName;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String description;
    
    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDateTime eventDate;
    
    @NotBlank(message = "La ubicación es obligatoria")
    private String location;
    
    private Double latitude;
    private Double longitude;
    
    @NotNull(message = "El tipo de evento es obligatorio")
    private Event.EventType eventType;
    
    // Constructors
    public EventCreateDto() {}
    
    public EventCreateDto(String eventName, String description, LocalDateTime eventDate, 
                         String location, Event.EventType eventType) {
        this.eventName = eventName;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.eventType = eventType;
    }
    
    // Getters and Setters
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Event.EventType getEventType() { return eventType; }
    public void setEventType(Event.EventType eventType) { this.eventType = eventType; }
}
