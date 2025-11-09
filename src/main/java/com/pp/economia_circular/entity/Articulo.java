package com.pp.economia_circular.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "articulos")
public class Articulo {

    // Getters y Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 200)
    private String titulo;
    
    @NotBlank
    @Size(max = 1000)
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    private CategoriaArticulo categoria;
    
    @Enumerated(EnumType.STRING)
    private CondicionArticulo condicion;
    
    @Enumerated(EnumType.STRING)
    private EstadoArticulo estado = EstadoArticulo.DISPONIBLE;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;
    
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
    
    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImagenArticulo> imagenes;
    
    @OneToMany(mappedBy = "articuloSolicitado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SolicitudIntercambio> solicitudesIntercambio;
    
    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VistaArticulo> vistas;
    
    // Constructores
    public Articulo() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }
    
    public Articulo(String titulo, String descripcion, CategoriaArticulo categoria, 
                   CondicionArticulo condicion, Usuario usuario) {
        this();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.condicion = condicion;
        this.usuario = usuario;
    }

    public void setId(Long id) { this.id = id; }

    @PreUpdate
    public void preActualizar() {
        this.actualizadoEn = LocalDateTime.now();
    }
    @Getter
    public enum CategoriaArticulo {
        ELECTRONICOS, ROPA, LIBROS, MUEBLES, HERRAMIENTAS, 
        DEPORTES, DECORACION_HOGAR, COCINA, JARDIN, AUTOMOTRIZ,
        JUGUETES, SUMINISTROS_ARTE, INSTRUMENTOS_MUSICALES, OTROS,
    }
    @Getter
    public enum CondicionArticulo {
        NUEVO, USADO, REACONDICIONADO, AVERIADO
    }
    @Getter
    public enum EstadoArticulo {
        DISPONIBLE,
        INTERCAMBIADO,
        RESERVADO,
        DONADO,
        CANCELADO,
        VENDIDO,
        PAUSADO
    }
}
