package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.Articulo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class ArticleCreateDto {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @NotNull(message = "La categoría es obligatoria")
    private Articulo.CategoriaArticulo category;

    @Size(max = 100)
    private String subcategoria;

    @Size(max = 100)
    private String marca;

    @Size(max = 100)
    private String modelo;

    @NotNull(message = "La condición es obligatoria")
    private Articulo.CondicionArticulo condition;

    // Nuevo campo
    private Articulo.EstadoArticulo estado;

    private List<String> etiquetas;

    public ArticleCreateDto() {}

    public ArticleCreateDto(String title, String description,
                            Articulo.CategoriaArticulo category,
                            Articulo.CondicionArticulo condition) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.condition = condition;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Articulo.CategoriaArticulo getCategory() { return category; }
    public void setCategory(Articulo.CategoriaArticulo category) { this.category = category; }

    public Articulo.CondicionArticulo getCondition() { return condition; }
    public void setCondition(Articulo.CondicionArticulo condition) { this.condition = condition; }

    public Articulo.EstadoArticulo getEstado() { return estado; }
    public void setEstado(Articulo.EstadoArticulo estado) { this.estado = estado; }

    public String getSubcategoria() { return subcategoria; }
    public void setSubcategoria(String subcategoria) { this.subcategoria = subcategoria; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public List<String> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<String> etiquetas) { this.etiquetas = etiquetas; }
}
