package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.RecyclingCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecyclingCenterRepository extends JpaRepository<RecyclingCenter, Long> {
    
    List<RecyclingCenter> findByCenterType(RecyclingCenter.CenterType centerType);
    
    List<RecyclingCenter> findByStatus(RecyclingCenter.CenterStatus status);
    
    List<RecyclingCenter> findByCenterTypeAndStatus(RecyclingCenter.CenterType centerType, 
                                                    RecyclingCenter.CenterStatus status);

    List<RecyclingCenter> findAllByStatus(RecyclingCenter.CenterStatus status);

    @Query("SELECT rc FROM RecyclingCenter rc WHERE " +
           "(:latitude IS NULL OR :longitude IS NULL OR " +
           "6371 * acos(cos(radians(:latitude)) * cos(radians(rc.latitude)) * " +
           "cos(radians(rc.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(rc.latitude))) <= :radius)")
    List<RecyclingCenter> findNearbyCenters(@Param("latitude") Double latitude, 
                                           @Param("longitude") Double longitude, 
                                           @Param("radius") Double radius);
}
