package com.pp.economia_circular.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "soporte_chats")
public class SoporteChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String asunto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoChat estado = EstadoChat.ABIERTO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadChat prioridad = PrioridadChat.MEDIA;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("creadoEn ASC")
    private List<MensajeSoporte> mensajes = new ArrayList<>();

    public SoporteChat() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    public void preActualizar() {
        this.actualizadoEn = LocalDateTime.now();
    }

    public enum EstadoChat { ABIERTO, EN_PROGRESO, RESUELTO, CERRADO }
    public enum PrioridadChat { BAJA, MEDIA, ALTA }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public EstadoChat getEstado() { return estado; }
    public void setEstado(EstadoChat estado) { this.estado = estado; }

    public PrioridadChat getPrioridad() { return prioridad; }
    public void setPrioridad(PrioridadChat prioridad) { this.prioridad = prioridad; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public List<MensajeSoporte> getMensajes() { return mensajes; }
    public void setMensajes(List<MensajeSoporte> mensajes) { this.mensajes = mensajes; }
}
