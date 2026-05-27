package com.pp.economia_circular.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "notificaciones",
        indexes = {
                @Index(name = "idx_notificaciones_destinatario", columnList = "destinatario_id"),
                @Index(name = "idx_notificaciones_leida", columnList = "leida"),
                @Index(name = "idx_notificaciones_creado_en", columnList = "creado_en")
        })
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "contrasena", "foto"})
    private Usuario destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emisor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "contrasena", "foto"})
    private Usuario emisor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private TipoNotificacion tipo;

    @Column(length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "referencia_tipo", length = 50)
    private ReferenciaTipo referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(nullable = false)
    private boolean leida = false;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "leido_en")
    private LocalDateTime leidoEn;

    public Notificacion() {
        this.creadoEn = LocalDateTime.now();
    }

    public enum TipoNotificacion {
        MENSAJE_NUEVO,
        VALORACION_NUEVA,
        SOLICITUD_INTERCAMBIO,
        SISTEMA
    }

    public enum ReferenciaTipo {
        MENSAJE,
        VALORACION,
        ARTICULO,
        SOLICITUD,
        USUARIO
    }
}
