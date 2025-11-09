package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.Mensaje;
import java.time.LocalDateTime;

public class MensajeDto {

    private Long id;
    private String contenido;
    private Long remitenteId;
    private String remitenteEmail;
    private Long destinatarioId;
    private String destinatarioEmail;
    private Long articuloId;
    private String estado;
    private LocalDateTime creadoEn;
    private LocalDateTime leidoEn;

    public MensajeDto() {}

    public MensajeDto(Mensaje mensaje) {
        this.id = mensaje.getId();
        this.contenido = mensaje.getContenido();
        this.estado = mensaje.getEstado().name();
        this.creadoEn = mensaje.getCreadoEn();
        this.leidoEn = mensaje.getLeidoEn();

        if (mensaje.getRemitente() != null) {
            this.remitenteId = mensaje.getRemitente().getId();
            this.remitenteEmail = mensaje.getRemitente().getEmail();
        }

        if (mensaje.getDestinatario() != null) {
            this.destinatarioId = mensaje.getDestinatario().getId();
            this.destinatarioEmail = mensaje.getDestinatario().getEmail();
        }

        if (mensaje.getArticulo() != null) {
            this.articuloId = mensaje.getArticulo().getId();
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Long getRemitenteId() { return remitenteId; }
    public void setRemitenteId(Long remitenteId) { this.remitenteId = remitenteId; }

    public String getRemitenteEmail() { return remitenteEmail; }
    public void setRemitenteEmail(String remitenteEmail) { this.remitenteEmail = remitenteEmail; }

    public Long getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }

    public String getDestinatarioEmail() { return destinatarioEmail; }
    public void setDestinatarioEmail(String destinatarioEmail) { this.destinatarioEmail = destinatarioEmail; }

    public Long getArticuloId() { return articuloId; }
    public void setArticuloId(Long articuloId) { this.articuloId = articuloId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getLeidoEn() { return leidoEn; }
    public void setLeidoEn(LocalDateTime leidoEn) { this.leidoEn = leidoEn; }
}
