package com.pp.economia_circular.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Un usuario sigue a un oferente (para ver lo que publica) o a un evento/feria
 * (para recibir avisos de cambios de fecha o novedades).
 */
@Getter
@Setter
@Entity
@Table(name = "seguimientos",
        uniqueConstraints = @UniqueConstraint(name = "uk_seguimiento",
                columnNames = {"usuario_id", "tipo", "objetivo_id"}),
        indexes = @Index(name = "idx_seguimientos_objetivo", columnList = "tipo, objetivo_id"))
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TipoSeguimiento tipo;

    /** Id del usuario oferente o del evento seguido, según el tipo. */
    @Column(name = "objetivo_id", nullable = false)
    private Long objetivoId;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Seguimiento() {}

    public Seguimiento(Usuario usuario, TipoSeguimiento tipo, Long objetivoId) {
        this.usuario = usuario;
        this.tipo = tipo;
        this.objetivoId = objetivoId;
    }

    public enum TipoSeguimiento {
        OFERENTE, EVENTO
    }
}
