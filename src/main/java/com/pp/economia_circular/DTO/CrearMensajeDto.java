package com.pp.economia_circular.DTO;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CrearMensajeDto {
    
    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(max = 1000, message = "El mensaje no puede exceder 1000 caracteres")
    private String contenido;
    
    private Long destinatarioId;
    private Long articuloId;
    
    // Constructores
    public CrearMensajeDto() {}
    
    public CrearMensajeDto(String contenido, Long destinatarioId) {
        this.contenido = contenido;
        this.destinatarioId = destinatarioId;
    }
    
    public CrearMensajeDto(String contenido, Long destinatarioId, Long articuloId) {
        this.contenido = contenido;
        this.destinatarioId = destinatarioId;
        this.articuloId = articuloId;
    }
    
    // Getters y Setters
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    
    public Long getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }
    
    public Long getArticuloId() { return articuloId; }
    public void setArticuloId(Long articuloId) { this.articuloId = articuloId; }
}
