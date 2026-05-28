package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.MensajeSoporte;

import java.time.LocalDateTime;

public class MensajeSoporteResponseDto {

    private Long id;
    private Long chatId;
    private Long emisorId;
    private String emisorEmail;
    private String emisorNombre;
    private boolean esAdmin;
    private String contenido;
    private boolean leido;
    private LocalDateTime creadoEn;

    public MensajeSoporteResponseDto() {}

    public MensajeSoporteResponseDto(MensajeSoporte m) {
        this.id = m.getId();
        this.chatId = m.getChat() != null ? m.getChat().getId() : null;
        this.contenido = m.getContenido();
        this.leido = m.isLeido();
        this.creadoEn = m.getCreadoEn();
        if (m.getEmisor() != null) {
            this.emisorId = m.getEmisor().getId();
            this.emisorEmail = m.getEmisor().getEmail();
            String nombre = (m.getEmisor().getNombre() != null ? m.getEmisor().getNombre() : "")
                    + (m.getEmisor().getApellido() != null ? " " + m.getEmisor().getApellido() : "");
            this.emisorNombre = nombre.trim();
            String rol = m.getEmisor().getRol();
            this.esAdmin = rol != null && rol.contains("ADMIN");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Long getEmisorId() { return emisorId; }
    public void setEmisorId(Long emisorId) { this.emisorId = emisorId; }

    public String getEmisorEmail() { return emisorEmail; }
    public void setEmisorEmail(String emisorEmail) { this.emisorEmail = emisorEmail; }

    public String getEmisorNombre() { return emisorNombre; }
    public void setEmisorNombre(String emisorNombre) { this.emisorNombre = emisorNombre; }

    public boolean isEsAdmin() { return esAdmin; }
    public void setEsAdmin(boolean esAdmin) { this.esAdmin = esAdmin; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
