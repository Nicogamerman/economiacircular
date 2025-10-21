package com.pp.economia_circular.DTO;


import com.pp.economia_circular.entity.Article;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ArticleCreateDto {
    
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String title;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;
    
    @NotNull(message = "La categoría es obligatoria")
    private Article.ArticleCategory category;
    
    @NotNull(message = "La condición es obligatoria")
    private Article.ArticleCondition condition;
    
    // Constructors
    public ArticleCreateDto() {}
    
    public ArticleCreateDto(String title, String description, Article.ArticleCategory category, 
                           Article.ArticleCondition condition) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.condition = condition;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Article.ArticleCategory getCategory() { return category; }
    public void setCategory(Article.ArticleCategory category) { this.category = category; }
    
    public Article.ArticleCondition getCondition() { return condition; }
    public void setCondition(Article.ArticleCondition condition) { this.condition = condition; }
}
