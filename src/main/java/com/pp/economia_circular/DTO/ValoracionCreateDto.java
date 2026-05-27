package com.pp.economia_circular.DTO;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ValoracionCreateDto {

    @NotNull
    private Long valoradoId;

    private Long articuloId;

    @NotNull
    @Min(value = 1, message = "El puntaje mínimo es 1")
    @Max(value = 5, message = "El puntaje máximo es 5")
    private Integer puntaje;

    @Size(max = 1000)
    private String comentario;

    public ValoracionCreateDto() {}

    public Long getValoradoId() { return valoradoId; }
    public void setValoradoId(Long valoradoId) { this.valoradoId = valoradoId; }

    public Long getArticuloId() { return articuloId; }
    public void setArticuloId(Long articuloId) { this.articuloId = articuloId; }

    public Integer getPuntaje() { return puntaje; }
    public void setPuntaje(Integer puntaje) { this.puntaje = puntaje; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
