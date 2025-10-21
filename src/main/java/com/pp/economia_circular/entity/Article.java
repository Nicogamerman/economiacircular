package com.pp.economia_circular.entity;


import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "articles")
public class Article {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 200)
    private String title;
    
    @NotBlank
    @Size(max = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    private ArticleCategory category;
    
    @Enumerated(EnumType.STRING)
    private ArticleCondition condition;
    
    @Enumerated(EnumType.STRING)
    private ArticleStatus status = ArticleStatus.AVAILABLE;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Usuario user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ArticleImage> images;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExchangeRequest> exchangeRequests;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ArticleView> views;
    
    // Constructors
    public Article() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Article(String title, String description, ArticleCategory category, 
                   ArticleCondition condition, Usuario user) {
        this();
        this.title = title;
        this.description = description;
        this.category = category;
        this.condition = condition;
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ArticleCategory getCategory() { return category; }
    public void setCategory(ArticleCategory category) { this.category = category; }
    
    public ArticleCondition getCondition() { return condition; }
    public void setCondition(ArticleCondition condition) { this.condition = condition; }
    
    public ArticleStatus getStatus() { return status; }
    public void setStatus(ArticleStatus status) { this.status = status; }
    
    public Usuario getUser() { return user; }
    public void setUser(Usuario user) { this.user = user; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<ArticleImage> getImages() { return images; }
    public void setImages(List<ArticleImage> images) { this.images = images; }
    
    public List<ExchangeRequest> getExchangeRequests() { return exchangeRequests; }
    public void setExchangeRequests(List<ExchangeRequest> exchangeRequests) { this.exchangeRequests = exchangeRequests; }
    
    public List<ArticleView> getViews() { return views; }
    public void setViews(List<ArticleView> views) { this.views = views; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum ArticleCategory {
        ELECTRONICS, CLOTHING, BOOKS, FURNITURE, TOOLS, 
        SPORTS, HOME_DECOR, KITCHEN, GARDEN, AUTOMOTIVE,
        TOYS, ART_SUPPLIES, MUSICAL_INSTRUMENTS, OTHER
    }
    
    public enum ArticleCondition {
        NEW, LIKE_NEW, GOOD, FAIR, POOR
    }
    
    public enum ArticleStatus {
        AVAILABLE, EXCHANGED, RESERVED, DELETED
    }
}
