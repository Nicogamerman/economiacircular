package com.pp.economia_circular.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "politicas")
public class Politica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPolitica tipo;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String titulo;

    @NotBlank
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String contenido;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private int orden = 0;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public Politica() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    public void preActualizar() {
        this.actualizadoEn = LocalDateTime.now();
    }

    public enum TipoPolitica {
        POLITICA_PRIVACIDAD, TERMINOS_USO, REGLAS_COMUNIDAD, FAQ, OTRO
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoPolitica getTipo() { return tipo; }
    public void setTipo(TipoPolitica tipo) { this.tipo = tipo; }

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
