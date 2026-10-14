package com.pp.economia_circular.DTO.ia;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.pp.economia_circular.entity.Articulo;

import java.util.List;

/** Datos que la IA propone a partir de la foto de un artículo. El usuario los revisa antes de publicar. */
public class SugerenciaArticuloDto {

    @JsonPropertyDescription("Título breve y descriptivo para la publicación, en español, de hasta 80 caracteres")
    public String titulo;

    @JsonPropertyDescription("Descripción de 2 a 4 oraciones en español: qué es, para qué sirve y detalles visibles del estado")
    public String descripcion;

    @JsonPropertyDescription("Categoría que mejor corresponde al artículo")
    public Articulo.CategoriaArticulo categoria;

    @JsonPropertyDescription("Subcategoría en español, por ejemplo 'Bicicletas' o 'Sillas'. Vacío si no aplica")
    public String subcategoria;

    @JsonPropertyDescription("Marca si se ve en la imagen; vacío si no se distingue")
    public String marca;

    @JsonPropertyDescription("Modelo si se ve en la imagen; vacío si no se distingue")
    public String modelo;

    @JsonPropertyDescription("Estado aparente del artículo según la foto")
    public Articulo.CondicionArticulo condicion;

    @JsonPropertyDescription("Entre 3 y 8 palabras clave en español, en minúsculas, para que otros lo encuentren")
    public List<String> etiquetas;

    @JsonPropertyDescription("Qué tan seguro estás de la identificación, de 0 a 100")
    public Integer confianza;
}
