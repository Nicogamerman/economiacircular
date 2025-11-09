package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.Articulo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleUserDto {
    private Long id;
    private String titulo;
    private Articulo.CategoriaArticulo categoria;
    private Articulo.EstadoArticulo estado;
    private Articulo.CondicionArticulo condicion;
    private String descripcion;
    private String usuarioEmail;
}
