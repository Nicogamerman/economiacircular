package com.pp.economia_circular.entity;


import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "recycling_centers")
public class RecyclingCenter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotBlank
    private String address;
    
    @NotNull
    private Double latitude;
    
    @NotNull
    private Double longitude;
    
    @NotBlank
    private String phone;
    
    @NotBlank
    private String email;
    
    @Enumerated(EnumType.STRING)
    private CenterType centerType;
    
    @Enumerated(EnumType.STRING)
    private CenterStatus status = CenterStatus.ACTIVE;
    
    @Column(name = "opening_hours")
    private String openingHours;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public RecyclingCenter() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public RecyclingCenter(String name, String description, String address, 
                          Double latitude, Double longitude, String phone, 
                          String email, CenterType centerType) {
        this();
        this.name = name;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.email = email;
        this.centerType = centerType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public CenterType getCenterType() { return centerType; }
    public void setCenterType(CenterType centerType) { this.centerType = centerType; }
    
    public CenterStatus getStatus() { return status; }
    public void setStatus(CenterStatus status) { this.status = status; }
    
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum CenterType {
        RECYCLING_CENTER, WASTE_COLLECTION_POINT, NGO, REPAIR_WORKSHOP
    }
    
    public enum CenterStatus {
        ACTIVE, INACTIVE, TEMPORARILY_CLOSED
    }
}
