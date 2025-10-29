package com.pp.economia_circular.entity;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_intercambio")
public class SolicitudIntercambio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_solicitado_id")
    private Articulo articuloSolicitado;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_ofrecido_id")
    private Articulo articuloOfrecido;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;
    
    @Enumerated(EnumType.STRING)
    private EstadoIntercambio estado = EstadoIntercambio.PENDIENTE;
    
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;
    
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
    
    // Constructores
    public SolicitudIntercambio() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }
    
    public SolicitudIntercambio(Articulo articuloSolicitado, Articulo articuloOfrecido, Usuario solicitante) {
        this();
        this.articuloSolicitado = articuloSolicitado;
        this.articuloOfrecido = articuloOfrecido;
        this.solicitante = solicitante;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Articulo getArticuloSolicitado() { return articuloSolicitado; }
    public void setArticuloSolicitado(Articulo articuloSolicitado) { this.articuloSolicitado = articuloSolicitado; }
    
    public Articulo getArticuloOfrecido() { return articuloOfrecido; }
    public void setArticuloOfrecido(Articulo articuloOfrecido) { this.articuloOfrecido = articuloOfrecido; }
    
    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }
    
    public EstadoIntercambio getEstado() { return estado; }
    public void setEstado(EstadoIntercambio estado) { this.estado = estado; }
    
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
    
    @PreUpdate
    public void preActualizar() {
        this.actualizadoEn = LocalDateTime.now();
    }
    
    public enum EstadoIntercambio {
        PENDIENTE, ACEPTADO, RECHAZADO, COMPLETADO, CANCELADO
    }
}

