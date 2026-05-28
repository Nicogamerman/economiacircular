package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.Politica;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PoliticaCreateDto {

    @NotNull(message = "El tipo es obligatorio")
    private Politica.TipoPolitica tipo;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200)
    private String titulo;

    @NotBlank(message = "El contenido es obligatorio")
    private String contenido;

    private boolean activo = true;

    private int orden = 0;

    public Politica.TipoPolitica getTipo() { return tipo; }
    public void setTipo(Politica.TipoPolitica tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
}
