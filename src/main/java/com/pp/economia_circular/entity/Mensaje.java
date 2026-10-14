package com.pp.economia_circular.entity;


import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "mensajes")
public class Mensaje {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String contenido;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario remitente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Articulo articulo;


    @Enumerated(EnumType.STRING)
    private EstadoMensaje estado = EstadoMensaje.NO_LEIDO;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TipoMensaje tipo = TipoMensaje.TEXTO;

    /** Duración en segundos (solo mensajes de audio). */
    @Column(name = "audio_duracion_seg")
    private Integer audioDuracionSeg;
    
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

    public TipoMensaje getTipo() { return tipo != null ? tipo : TipoMensaje.TEXTO; }
    public void setTipo(TipoMensaje tipo) { this.tipo = tipo; }

    public Integer getAudioDuracionSeg() { return audioDuracionSeg; }
    public void setAudioDuracionSeg(Integer audioDuracionSeg) { this.audioDuracionSeg = audioDuracionSeg; }
    
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    
    public LocalDateTime getLeidoEn() { return leidoEn; }
    public void setLeidoEn(LocalDateTime leidoEn) { this.leidoEn = leidoEn; }
    
    public enum TipoMensaje {
        TEXTO, AUDIO
    }

    public enum EstadoMensaje {
        NO_LEIDO, LEIDO, ELIMINADO
    }
}
