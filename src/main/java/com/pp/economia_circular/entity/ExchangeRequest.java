package com.pp.economia_circular.entity;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_requests")
public class ExchangeRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_article_id")
    private Article requestedArticle;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_article_id")
    private Article offeredArticle;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private Usuario requester;
    
    @Enumerated(EnumType.STRING)
    private ExchangeStatus status = ExchangeStatus.PENDING;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public ExchangeRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ExchangeRequest(Article requestedArticle, Article offeredArticle, Usuario requester) {
        this();
        this.requestedArticle = requestedArticle;
        this.offeredArticle = offeredArticle;
        this.requester = requester;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Article getRequestedArticle() { return requestedArticle; }
    public void setRequestedArticle(Article requestedArticle) { this.requestedArticle = requestedArticle; }
    
    public Article getOfferedArticle() { return offeredArticle; }
    public void setOfferedArticle(Article offeredArticle) { this.offeredArticle = offeredArticle; }
    
    public Usuario getRequester() { return requester; }
    public void setRequester(Usuario requester) { this.requester = requester; }
    
    public ExchangeStatus getStatus() { return status; }
    public void setStatus(ExchangeStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum ExchangeStatus {
        PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    }
}
