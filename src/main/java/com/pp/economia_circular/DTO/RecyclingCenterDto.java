package com.pp.economia_circular.DTO;



import com.pp.economia_circular.entity.RecyclingCenter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class RecyclingCenterDto {
    
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String description;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String address;
    
    @NotNull(message = "La latitud es obligatoria")
    private Double latitude;
    
    @NotNull(message = "La longitud es obligatoria")
    private Double longitude;
    
    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    @NotNull(message = "El tipo de centro es obligatorio")
    private RecyclingCenter.CenterType centerType;
    
    private RecyclingCenter.CenterStatus status;
    private String openingHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public RecyclingCenterDto() {}
    
    public RecyclingCenterDto(String name, String description, String address, 
                            Double latitude, Double longitude, String phone, 
                            String email, RecyclingCenter.CenterType centerType) {
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
    
    public RecyclingCenter.CenterType getCenterType() { return centerType; }
    public void setCenterType(RecyclingCenter.CenterType centerType) { this.centerType = centerType; }
    
    public RecyclingCenter.CenterStatus getStatus() { return status; }
    public void setStatus(RecyclingCenter.CenterStatus status) { this.status = status; }
    
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
