package com.pp.economia_circular.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Datos del video sin el contenido binario. */
@Getter
@Setter
public class VideoArticuloDto {

    private Long id;
    private Long articuloId;
    private String nombreArchivo;
    private String contentType;
    private Long tamanoBytes;
    private boolean conAudio;
    private LocalDateTime creadoEn;

    public VideoArticuloDto() {}

    public VideoArticuloDto(Long id, Long articuloId, String nombreArchivo, String contentType,
                            Long tamanoBytes, boolean conAudio, LocalDateTime creadoEn) {
        this.id = id;
        this.articuloId = articuloId;
        this.nombreArchivo = nombreArchivo;
        this.contentType = contentType;
        this.tamanoBytes = tamanoBytes;
        this.conAudio = conAudio;
        this.creadoEn = creadoEn;
    }
}
