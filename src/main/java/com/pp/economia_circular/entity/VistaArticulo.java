package com.pp.economia_circular.entity;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "vistas_articulos")
public class VistaArticulo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    private String direccionIp;
    private String agenteUsuario;
    
    @Column(name = "visto_en")
    private LocalDateTime vistoEn;
    
    // Constructores
    public VistaArticulo() {
        this.vistoEn = LocalDateTime.now();
    }
    
    public VistaArticulo(Articulo articulo, Usuario usuario) {
        this();
        this.articulo = articulo;
        this.usuario = usuario;
    }
    
    public VistaArticulo(Articulo articulo, String direccionIp, String agenteUsuario) {
        this();
        this.articulo = articulo;
        this.direccionIp = direccionIp;
        this.agenteUsuario = agenteUsuario;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public String getDireccionIp() { return direccionIp; }
    public void setDireccionIp(String direccionIp) { this.direccionIp = direccionIp; }
    
    public String getAgenteUsuario() { return agenteUsuario; }
    public void setAgenteUsuario(String agenteUsuario) { this.agenteUsuario = agenteUsuario; }
    
    public LocalDateTime getVistoEn() { return vistoEn; }
    public void setVistoEn(LocalDateTime vistoEn) { this.vistoEn = vistoEn; }
}

