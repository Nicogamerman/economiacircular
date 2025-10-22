package com.pp.economia_circular.DTO;


import com.pp.economia_circular.entity.Articulo;


public class ArticleSearchDto {
    
    private String title;
    private Articulo.CategoriaArticulo category;
    private Articulo.CondicionArticulo condition;
    
    // Constructors
    public ArticleSearchDto() {}
    
    public ArticleSearchDto(String title, Articulo.CategoriaArticulo category, Articulo.CondicionArticulo condition) {
        this.title = title;
        this.category = category;
        this.condition = condition;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Articulo.CategoriaArticulo getCategory() { return category; }
    public void setCategory(Articulo.CategoriaArticulo category) { this.category = category; }
    
    public Articulo.CondicionArticulo getCondition() { return condition; }
    public void setCondition(Articulo.CondicionArticulo condition) { this.condition = condition; }
}
