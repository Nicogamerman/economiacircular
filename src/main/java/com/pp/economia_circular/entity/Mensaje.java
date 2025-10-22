package com.pp.economia_circular.entity;


import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensaje {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String contenido;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id")
    private Usuario remitente;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;
    
    @Enumerated(EnumType.STRING)
    private EstadoMensaje estado = EstadoMensaje.NO_LEIDO;
    
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;
    
    @Column(name = "leido_en")
    private LocalDateTime leidoEn;
    
    // Constructores
    public Mensaje() {
        this.creadoEn = LocalDateTime.now();
    }
    
    public Mensaje(String contenido, Usuario remitente, Usuario destinatario, Articulo articulo) {
        this();
        this.contenido = contenido;
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.articulo = articulo;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    
    public Usuario getRemitente() { return remitente; }
    public void setRemitente(Usuario remitente) { this.remitente = remitente; }
    
    public Usuario getDestinatario() { return destinatario; }
    public void setDestinatario(Usuario destinatario) { this.destinatario = destinatario; }
    
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }
    
    public EstadoMensaje getEstado() { return estado; }
    public void setEstado(EstadoMensaje estado) { this.estado = estado; }
    
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    
    public LocalDateTime getLeidoEn() { return leidoEn; }
    public void setLeidoEn(LocalDateTime leidoEn) { this.leidoEn = leidoEn; }
    
    public enum EstadoMensaje {
        NO_LEIDO, LEIDO, ELIMINADO
    }
}
