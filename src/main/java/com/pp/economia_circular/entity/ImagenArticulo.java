package com.pp.economia_circular.entity;


import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "imagenes_articulos")
public class ImagenArticulo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String urlImagen;
    
    @NotBlank
    private String nombreArchivo;
    
    private String descripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;
    
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;
    
    // Constructores
    public ImagenArticulo() {
        this.creadoEn = LocalDateTime.now();
    }
    
    public ImagenArticulo(String urlImagen, String nombreArchivo, Articulo articulo) {
        this();
        this.urlImagen = urlImagen;
        this.nombreArchivo = nombreArchivo;
        this.articulo = articulo;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
    
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }
    
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}

