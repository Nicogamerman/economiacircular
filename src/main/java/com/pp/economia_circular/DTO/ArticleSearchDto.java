package com.pp.economia_circular.DTO;


import com.pp.economia_circular.entity.Articulo;


public class ArticleSearchDto {

    private String title;
    private Articulo.CategoriaArticulo category;
    private Articulo.CondicionArticulo condition;
    private String subcategoria;
    private String marca;
    private String tag;
    private String q;

    public ArticleSearchDto() {}

    public ArticleSearchDto(String title, Articulo.CategoriaArticulo category, Articulo.CondicionArticulo condition) {
        this.title = title;
        this.category = category;
        this.condition = condition;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Articulo.CategoriaArticulo getCategory() { return category; }
    public void setCategory(Articulo.CategoriaArticulo category) { this.category = category; }

    public Articulo.CondicionArticulo getCondition() { return condition; }
    public void setCondition(Articulo.CondicionArticulo condition) { this.condition = condition; }

    public String getSubcategoria() { return subcategoria; }
    public void setSubcategoria(String subcategoria) { this.subcategoria = subcategoria; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
}
