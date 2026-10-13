package com.pp.economia_circular.DTO;


import com.pp.economia_circular.entity.Articulo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponseDto {

    private Long id;
    private String title;
    private String description;
    private Articulo.CategoriaArticulo category;
    private String subcategoria;
    private String marca;
    private String modelo;
    private Articulo.CondicionArticulo condition;
    private Articulo.EstadoArticulo status;
    private List<String> etiquetas;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
