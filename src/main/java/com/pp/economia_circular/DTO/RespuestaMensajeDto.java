package com.pp.economia_circular.DTO;



import com.pp.economia_circular.entity.Mensaje;

import java.time.LocalDateTime;

public class RespuestaMensajeDto {
    
    private Long id;
    private String contenido;
    private Long remitenteId;
    private String nombreRemitente;
    private Long destinatarioId;
    private String nombreDestinatario;
    private Long articuloId;
    private Mensaje.EstadoMensaje estado;
    private LocalDateTime creadoEn;
    private LocalDateTime leidoEn;
    
    // Constructores
    public RespuestaMensajeDto() {}
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    
    public Long getRemitenteId() { return remitenteId; }
    public void setRemitenteId(Long remitenteId) { this.remitenteId = remitenteId; }
    
    public String getNombreRemitente() { return nombreRemitente; }
    public void setNombreRemitente(String nombreRemitente) { this.nombreRemitente = nombreRemitente; }
    
    public Long getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }
    
    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }
    
    public Long getArticuloId() { return articuloId; }
    public void setArticuloId(Long articuloId) { this.articuloId = articuloId; }
    
    public Mensaje.EstadoMensaje getEstado() { return estado; }
    public void setEstado(Mensaje.EstadoMensaje estado) { this.estado = estado; }
    
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    
    public LocalDateTime getLeidoEn() { return leidoEn; }
    public void setLeidoEn(LocalDateTime leidoEn) { this.leidoEn = leidoEn; }
}
