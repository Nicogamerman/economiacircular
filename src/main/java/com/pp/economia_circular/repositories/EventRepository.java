package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByEventType(Event.EventType eventType);
    
    List<Event> findByStatus(Event.EventStatus status);
    
    List<Event> findByEventTypeAndStatus(Event.EventType eventType, Event.EventStatus status);
    
    List<Event> findByEventDateAfterAndStatus(LocalDateTime eventDate, Event.EventStatus status);
    
    List<Event> findByEventDateBeforeAndStatus(LocalDateTime eventDate, Event.EventStatus status);
    
    @Query("SELECT e FROM Event e WHERE " +
           "(:latitude IS NULL OR :longitude IS NULL OR " +
           "6371 * acos(cos(radians(:latitude)) * cos(radians(e.latitude)) * " +
           "cos(radians(e.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(e.latitude))) <= :radius) AND " +
           "e.status = 'ACTIVE'")
    List<Event> findNearbyEvents(@Param("latitude") Double latitude, 
                                 @Param("longitude") Double longitude, 
                                 @Param("radius") Double radius);
}
