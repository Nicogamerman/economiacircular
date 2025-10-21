package com.pp.economia_circular.DTO;


import com.pp.economia_circular.entity.Article;

import java.time.LocalDateTime;

public class ArticleResponseDto {
    
    private Long id;
    private String title;
    private String description;
    private Article.ArticleCategory category;
    private Article.ArticleCondition condition;
    private Article.ArticleStatus status;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public ArticleResponseDto() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Article.ArticleCategory getCategory() { return category; }
    public void setCategory(Article.ArticleCategory category) { this.category = category; }
    
    public Article.ArticleCondition getCondition() { return condition; }
    public void setCondition(Article.ArticleCondition condition) { this.condition = condition; }
    
    public Article.ArticleStatus getStatus() { return status; }
    public void setStatus(Article.ArticleStatus status) { this.status = status; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
