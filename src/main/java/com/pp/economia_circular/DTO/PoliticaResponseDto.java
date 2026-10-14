package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.Politica;

import java.time.LocalDateTime;

public class PoliticaResponseDto {

    private Long id;
    private Politica.TipoPolitica tipo;
    private String titulo;
    private String contenido;
    private boolean activo;
    private int orden;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public PoliticaResponseDto() {}

    public static PoliticaResponseDto from(Politica p) {
        PoliticaResponseDto dto = new PoliticaResponseDto();
        dto.setId(p.getId());
        dto.setTipo(p.getTipo());
        dto.setTitulo(p.getTitulo());
        dto.setContenido(p.getContenido());
        dto.setActivo(p.isActivo());
        dto.setOrden(p.getOrden());
        dto.setCreadoEn(p.getCreadoEn());
        dto.setActualizadoEn(p.getActualizadoEn());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
