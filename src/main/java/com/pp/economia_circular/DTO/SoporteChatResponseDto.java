package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.SoporteChat;

import java.time.LocalDateTime;

public class SoporteChatResponseDto {

    private Long id;
    private Long usuarioId;
    private String usuarioEmail;
    private String usuarioNombre;
    private String asunto;
    private SoporteChat.EstadoChat estado;
    private SoporteChat.PrioridadChat prioridad;
    private long mensajesNoLeidos;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public SoporteChatResponseDto() {}

    public static SoporteChatResponseDto from(SoporteChat c, long noLeidos) {
        SoporteChatResponseDto dto = new SoporteChatResponseDto();
        dto.setId(c.getId());
        dto.setAsunto(c.getAsunto());
        dto.setEstado(c.getEstado());
        dto.setPrioridad(c.getPrioridad());
        dto.setCreadoEn(c.getCreadoEn());
        dto.setActualizadoEn(c.getActualizadoEn());
        dto.setMensajesNoLeidos(noLeidos);
        if (c.getUsuario() != null) {
            dto.setUsuarioId(c.getUsuario().getId());
            dto.setUsuarioEmail(c.getUsuario().getEmail());
            String nombre = (c.getUsuario().getNombre() != null ? c.getUsuario().getNombre() : "")
                    + (c.getUsuario().getApellido() != null ? " " + c.getUsuario().getApellido() : "");
            dto.setUsuarioNombre(nombre.trim());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public SoporteChat.EstadoChat getEstado() { return estado; }
    public void setEstado(SoporteChat.EstadoChat estado) { this.estado = estado; }

    public SoporteChat.PrioridadChat getPrioridad() { return prioridad; }
    public void setPrioridad(SoporteChat.PrioridadChat prioridad) { this.prioridad = prioridad; }

    public long getMensajesNoLeidos() { return mensajesNoLeidos; }
    public void setMensajesNoLeidos(long mensajesNoLeidos) { this.mensajesNoLeidos = mensajesNoLeidos; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
