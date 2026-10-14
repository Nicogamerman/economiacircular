package com.pp.economia_circular.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Pedido de devolución sobre un intercambio completado. Si la otra parte lo acepta,
 * el intercambio pasa a DEVUELTO y ambos artículos vuelven a estar disponibles.
 */
@Getter
@Setter
@Entity
@Table(name = "devoluciones", indexes = @Index(name = "idx_devoluciones_solicitud", columnList = "solicitud_id"))
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudIntercambio solicitud;

    /** Participante del intercambio que pide la devolución. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDevolucion estado = EstadoDevolucion.PENDIENTE;

    @Column(length = 1000)
    private String respuesta;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "resuelto_en")
    private LocalDateTime resueltoEn;

    public enum EstadoDevolucion {
        PENDIENTE, ACEPTADA, RECHAZADA, CANCELADA
    }
}
