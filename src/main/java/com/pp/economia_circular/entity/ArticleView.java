package com.pp.economia_circular.entity;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "article_views")
public class ArticleView {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Usuario user;
    
    private String ipAddress;
    private String userAgent;
    
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;
    
    // Constructors
    public ArticleView() {
        this.viewedAt = LocalDateTime.now();
    }
    
    public ArticleView(Article article, Usuario user) {
        this();
        this.article = article;
        this.user = user;
    }
    
    public ArticleView(Article article, String ipAddress, String userAgent) {
        this();
        this.article = article;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }
    
    public Usuario getUser() { return user; }
    public void setUser(Usuario user) { this.user = user; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}
