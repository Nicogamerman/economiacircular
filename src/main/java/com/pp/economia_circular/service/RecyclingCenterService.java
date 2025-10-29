package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.RecyclingCenterDto;
import com.pp.economia_circular.entity.RecyclingCenter;
import com.pp.economia_circular.repositories.RecyclingCenterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecyclingCenterService {
    
    @Autowired
    private RecyclingCenterRepository recyclingCenterRepository;
    
    public RecyclingCenterDto createCenter(RecyclingCenterDto centerDto) {
        RecyclingCenter center = new RecyclingCenter();
        center.setName(centerDto.getName());
        center.setDescription(centerDto.getDescription());
        center.setAddress(centerDto.getAddress());
        center.setLatitude(centerDto.getLatitude());
        center.setLongitude(centerDto.getLongitude());
        center.setPhone(centerDto.getPhone());
        center.setEmail(centerDto.getEmail());
        center.setCenterType(centerDto.getCenterType());
        center.setOpeningHours(centerDto.getOpeningHours());
        
        RecyclingCenter savedCenter = recyclingCenterRepository.save(center);
        return convertToDto(savedCenter);
    }
    
    public List<RecyclingCenterDto> getAllCenters() {
        return recyclingCenterRepository.findAllByStatus(RecyclingCenter.CenterStatus.ACTIVE).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<RecyclingCenterDto> getCentersByType(RecyclingCenter.CenterType centerType) {
        return recyclingCenterRepository.findByCenterTypeAndStatus(centerType, RecyclingCenter.CenterStatus.ACTIVE).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<RecyclingCenterDto> getCentersNearLocation(Double latitude, Double longitude, Double radiusKm) {
        // Implementación simplificada - en producción usarías una consulta espacial
        return recyclingCenterRepository.findAll().stream()
                .filter(center -> calculateDistance(latitude, longitude, 
                        center.getLatitude(), center.getLongitude()) <= radiusKm)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public RecyclingCenterDto updateCenter(Long id, RecyclingCenterDto centerDto) {
        RecyclingCenter center = recyclingCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Centro no encontrado"));
        
        center.setName(centerDto.getName());
        center.setDescription(centerDto.getDescription());
        center.setAddress(centerDto.getAddress());
        center.setLatitude(centerDto.getLatitude());
        center.setLongitude(centerDto.getLongitude());
        center.setPhone(centerDto.getPhone());
        center.setEmail(centerDto.getEmail());
        center.setCenterType(centerDto.getCenterType());
        center.setOpeningHours(centerDto.getOpeningHours());
        
        RecyclingCenter updatedCenter = recyclingCenterRepository.save(center);
        return convertToDto(updatedCenter);
    }
    
    public void deleteCenter(Long id) {
        RecyclingCenter center = recyclingCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Centro no encontrado"));
        
        center.setStatus(RecyclingCenter.CenterStatus.INACTIVE);
        recyclingCenterRepository.save(center);
    }

    public RecyclingCenter getById(Long id){

        return recyclingCenterRepository.findById(id).orElseThrow(() -> new RuntimeException("Centro no encontrado"));
    }

    private RecyclingCenterDto convertToDto(RecyclingCenter center) {
        RecyclingCenterDto dto = new RecyclingCenterDto();
        dto.setId(center.getId());
        dto.setName(center.getName());
        dto.setDescription(center.getDescription());
        dto.setAddress(center.getAddress());
        dto.setLatitude(center.getLatitude());
        dto.setLongitude(center.getLongitude());
        dto.setPhone(center.getPhone());
        dto.setEmail(center.getEmail());
        dto.setCenterType(center.getCenterType());
        dto.setStatus(center.getStatus());
        dto.setOpeningHours(center.getOpeningHours());
        dto.setCreatedAt(center.getCreatedAt());
        dto.setUpdatedAt(center.getUpdatedAt());
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
