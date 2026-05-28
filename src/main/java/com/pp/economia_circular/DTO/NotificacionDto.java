package com.pp.economia_circular.DTO;

import java.time.LocalDateTime;

public class NotificacionDto {

    private Long id;
    private String mensaje;
    private String tipo;
    private boolean leida;
    private Long articuloId;
    private String articuloTitulo;
    private LocalDateTime creadoEn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public Long getArticuloId() {
        return articuloId;
    }

    public void setArticuloId(Long articuloId) {
        this.articuloId = articuloId;
    }

    public String getArticuloTitulo() {
        return articuloTitulo;
    }

    public void setArticuloTitulo(String articuloTitulo) {
        this.articuloTitulo = articuloTitulo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
