package com.pp.economia_circular.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/** Video del producto (uno por artículo). El oferente elige si se reproduce con audio. */
@Getter
@Setter
@Entity
@Table(name = "videos_articulos",
        uniqueConstraints = @UniqueConstraint(name = "uk_video_articulo", columnNames = "articulo_id"))
public class VideoArticulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    /** false: el video se muestra silenciado aunque tenga pista de audio. */
    @Column(name = "con_audio", nullable = false)
    private boolean conAudio = true;

    @JsonIgnore
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contenido", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] contenido;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
