package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.ImagenArticulo;

import java.time.LocalDateTime;

public class ImagenArticuloResponseDto {

    private Long id;
    private Long articuloId;
    private String nombreArchivo;
    private String descripcion;
    private String contentType;
    private Long tamanoBytes;
    private String url;
    private LocalDateTime creadoEn;

    public ImagenArticuloResponseDto() {}

    public ImagenArticuloResponseDto(ImagenArticulo imagen, String baseUrl) {
        this.id = imagen.getId();
        this.nombreArchivo = imagen.getNombreArchivo();
        this.descripcion = imagen.getDescripcion();
        this.contentType = imagen.getContentType();
        this.tamanoBytes = imagen.getTamanoBytes();
        this.creadoEn = imagen.getCreadoEn();
        if (imagen.getArticulo() != null) {
            this.articuloId = imagen.getArticulo().getId();
            this.url = baseUrl + "/api/articles/" + this.articuloId + "/images/" + this.id + "/file";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArticuloId() { return articuloId; }
    public void setArticuloId(Long articuloId) { this.articuloId = articuloId; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
