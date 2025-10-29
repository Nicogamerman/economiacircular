package com.pp.economia_circular.DTO;

import java.time.LocalDateTime;
import java.util.Map;

public class ReportDto {
    
    private String title;
    private LocalDateTime generatedAt;
    private Map<String, Object> data;
    
    // Constructors
    public ReportDto() {
        this.generatedAt = LocalDateTime.now();
    }
    
    public ReportDto(String title) {
        this();
        this.title = title;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
