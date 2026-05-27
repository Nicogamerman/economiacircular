package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.Notificacion;

import java.time.LocalDateTime;

public class NotificacionResponseDto {

    private Long id;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String referenciaTipo;
    private Long referenciaId;
    private boolean leida;
    private Long emisorId;
    private String emisorNombre;
    private LocalDateTime creadoEn;
    private LocalDateTime leidoEn;

    public NotificacionResponseDto() {}

    public NotificacionResponseDto(Notificacion n) {
        this.id = n.getId();
        this.tipo = n.getTipo() != null ? n.getTipo().name() : null;
        this.titulo = n.getTitulo();
        this.mensaje = n.getMensaje();
        this.referenciaTipo = n.getReferenciaTipo() != null ? n.getReferenciaTipo().name() : null;
        this.referenciaId = n.getReferenciaId();
        this.leida = n.isLeida();
        this.creadoEn = n.getCreadoEn();
        this.leidoEn = n.getLeidoEn();
        if (n.getEmisor() != null) {
            this.emisorId = n.getEmisor().getId();
            String nombre = n.getEmisor().getNombre();
            String apellido = n.getEmisor().getApellido();
            String combinado = ((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "")).trim();
            this.emisorNombre = combinado.isEmpty() ? n.getEmisor().getEmail() : combinado;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getReferenciaTipo() { return referenciaTipo; }
    public void setReferenciaTipo(String referenciaTipo) { this.referenciaTipo = referenciaTipo; }

    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public Long getEmisorId() { return emisorId; }
    public void setEmisorId(Long emisorId) { this.emisorId = emisorId; }

    public String getEmisorNombre() { return emisorNombre; }
    public void setEmisorNombre(String emisorNombre) { this.emisorNombre = emisorNombre; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getLeidoEn() { return leidoEn; }
    public void setLeidoEn(LocalDateTime leidoEn) { this.leidoEn = leidoEn; }
}
