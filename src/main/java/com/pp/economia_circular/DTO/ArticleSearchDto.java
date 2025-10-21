package com.pp.economia_circular.DTO;


import com.pp.economia_circular.entity.Article;

public class ArticleSearchDto {
    
    private String title;
    private Article.ArticleCategory category;
    private Article.ArticleCondition condition;
    
    // Constructors
    public ArticleSearchDto() {}
    
    public ArticleSearchDto(String title, Article.ArticleCategory category, Article.ArticleCondition condition) {
        this.title = title;
        this.category = category;
        this.condition = condition;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Article.ArticleCategory getCategory() { return category; }
    public void setCategory(Article.ArticleCategory category) { this.category = category; }
    
    public Article.ArticleCondition getCondition() { return condition; }
    public void setCondition(Article.ArticleCondition condition) { this.condition = condition; }
}
