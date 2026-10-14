package com.pp.economia_circular.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Intento de verificación de identidad. Por privacidad NO se guardan las imágenes
 * del documento ni la selfie: solo el resultado del análisis.
 */
@Getter
@Setter
@Entity
@Table(name = "verificaciones_identidad", indexes = @Index(name = "idx_verif_identidad_usuario", columnList = "usuario_id"))
public class VerificacionIdentidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVerificacion estado;

    @Column(name = "tipo_documento", length = 100)
    private String tipoDocumento;

    private Integer confianza;

    @Column(length = 1000)
    private String motivo;

    /** Nota del administrador que resolvió una revisión manual. */
    @Column(name = "nota_revision", length = 1000)
    private String notaRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por_id")
    private Usuario revisadoPor;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "resuelto_en")
    private LocalDateTime resueltoEn;

    public enum EstadoVerificacion {
        APROBADA, RECHAZADA, REVISION_MANUAL
    }
}
