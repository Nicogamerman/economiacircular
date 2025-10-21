package com.pp.economia_circular.controller;


import com.pp.economia_circular.DTO.RecyclingCenterDto;
import com.pp.economia_circular.entity.RecyclingCenter;
import com.pp.economia_circular.service.RecyclingCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/recycling-centers")
@CrossOrigin(origins = "*")
public class RecyclingCenterController {
    
    @Autowired
    private RecyclingCenterService recyclingCenterService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCenter(@Valid @RequestBody RecyclingCenterDto centerDto) {
        try {
            RecyclingCenterDto center = recyclingCenterService.createCenter(centerDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(center);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllCenters() {
        try {
            List<RecyclingCenterDto> centers = recyclingCenterService.getAllCenters();
            return ResponseEntity.ok(centers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/type/{centerType}")
    public ResponseEntity<?> getCentersByType(@PathVariable RecyclingCenter.CenterType centerType) {
        try {
            List<RecyclingCenterDto> centers = recyclingCenterService.getCentersByType(centerType);
            return ResponseEntity.ok(centers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/nearby")
    public ResponseEntity<?> getCentersNearLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Double radiusKm) {
        try {
            List<RecyclingCenterDto> centers = recyclingCenterService.getCentersNearLocation(latitude, longitude, radiusKm);
            return ResponseEntity.ok(centers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCenter(@PathVariable Long id, @Valid @RequestBody RecyclingCenterDto centerDto) {
        try {
            RecyclingCenterDto center = recyclingCenterService.updateCenter(id, centerDto);
            return ResponseEntity.ok(center);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCenter(@PathVariable Long id) {
        try {
            recyclingCenterService.deleteCenter(id);
            return ResponseEntity.ok("Centro eliminado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
