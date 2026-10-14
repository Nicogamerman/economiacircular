package com.pp.economia_circular.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/** Tipo de evento (feria, taller, etc.) sobre el que un usuario quiere recibir avisos. */
@Getter
@Setter
@Entity
@Table(name = "intereses_eventos",
        uniqueConstraints = @UniqueConstraint(name = "uk_interes_evento",
                columnNames = {"usuario_id", "tipo_evento"}))
public class InteresEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", length = 30, nullable = false)
    private Event.EventType tipoEvento;

    public InteresEvento() {}

    public InteresEvento(Usuario usuario, Event.EventType tipoEvento) {
        this.usuario = usuario;
        this.tipoEvento = tipoEvento;
    }
}
