package com.pp.economia_circular.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/** Audio de un mensaje de chat. Tabla aparte para no traer el binario al listar mensajes. */
@Getter
@Setter
@Entity
@Table(name = "mensajes_audio",
        uniqueConstraints = @UniqueConstraint(name = "uk_mensaje_audio", columnNames = "mensaje_id"))
public class MensajeAudio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mensaje_id", nullable = false)
    private Mensaje mensaje;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "contenido", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] contenido;
}
