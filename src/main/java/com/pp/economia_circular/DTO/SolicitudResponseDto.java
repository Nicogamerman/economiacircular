package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.SolicitudIntercambio;

import java.time.LocalDateTime;

public class SolicitudResponseDto {

    private Long id;
    private SolicitudIntercambio.EstadoIntercambio estado;

    private Long articuloSolicitadoId;
    private String articuloSolicitadoTitulo;
    private Long propietarioArticuloSolicitadoId;
    private String propietarioArticuloSolicitadoEmail;

    private Long articuloOfrecidoId;
    private String articuloOfrecidoTitulo;

    private Long solicitanteId;
    private String solicitanteEmail;
    private String solicitanteNombre;

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public SolicitudResponseDto() {}

    public static SolicitudResponseDto from(SolicitudIntercambio s) {
        SolicitudResponseDto dto = new SolicitudResponseDto();
        dto.setId(s.getId());
        dto.setEstado(s.getEstado());
        dto.setCreadoEn(s.getCreadoEn());
        dto.setActualizadoEn(s.getActualizadoEn());

        if (s.getArticuloSolicitado() != null) {
            dto.setArticuloSolicitadoId(s.getArticuloSolicitado().getId());
            dto.setArticuloSolicitadoTitulo(s.getArticuloSolicitado().getTitulo());
            if (s.getArticuloSolicitado().getUsuario() != null) {
                dto.setPropietarioArticuloSolicitadoId(s.getArticuloSolicitado().getUsuario().getId());
                dto.setPropietarioArticuloSolicitadoEmail(s.getArticuloSolicitado().getUsuario().getEmail());
            }
        }

        if (s.getArticuloOfrecido() != null) {
            dto.setArticuloOfrecidoId(s.getArticuloOfrecido().getId());
            dto.setArticuloOfrecidoTitulo(s.getArticuloOfrecido().getTitulo());
        }

        if (s.getSolicitante() != null) {
            dto.setSolicitanteId(s.getSolicitante().getId());
            dto.setSolicitanteEmail(s.getSolicitante().getEmail());
            String nombre = (s.getSolicitante().getNombre() != null ? s.getSolicitante().getNombre() : "")
                    + (s.getSolicitante().getApellido() != null ? " " + s.getSolicitante().getApellido() : "");
            dto.setSolicitanteNombre(nombre.trim());
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SolicitudIntercambio.EstadoIntercambio getEstado() { return estado; }
    public void setEstado(SolicitudIntercambio.EstadoIntercambio estado) { this.estado = estado; }

    public Long getArticuloSolicitadoId() { return articuloSolicitadoId; }
    public void setArticuloSolicitadoId(Long articuloSolicitadoId) { this.articuloSolicitadoId = articuloSolicitadoId; }

    public String getArticuloSolicitadoTitulo() { return articuloSolicitadoTitulo; }
    public void setArticuloSolicitadoTitulo(String articuloSolicitadoTitulo) { this.articuloSolicitadoTitulo = articuloSolicitadoTitulo; }

    public Long getPropietarioArticuloSolicitadoId() { return propietarioArticuloSolicitadoId; }
    public void setPropietarioArticuloSolicitadoId(Long propietarioArticuloSolicitadoId) { this.propietarioArticuloSolicitadoId = propietarioArticuloSolicitadoId; }

    public String getPropietarioArticuloSolicitadoEmail() { return propietarioArticuloSolicitadoEmail; }
    public void setPropietarioArticuloSolicitadoEmail(String propietarioArticuloSolicitadoEmail) { this.propietarioArticuloSolicitadoEmail = propietarioArticuloSolicitadoEmail; }

    public Long getArticuloOfrecidoId() { return articuloOfrecidoId; }
    public void setArticuloOfrecidoId(Long articuloOfrecidoId) { this.articuloOfrecidoId = articuloOfrecidoId; }

    public String getArticuloOfrecidoTitulo() { return articuloOfrecidoTitulo; }
    public void setArticuloOfrecidoTitulo(String articuloOfrecidoTitulo) { this.articuloOfrecidoTitulo = articuloOfrecidoTitulo; }

    public Long getSolicitanteId() { return solicitanteId; }
    public void setSolicitanteId(Long solicitanteId) { this.solicitanteId = solicitanteId; }

    public String getSolicitanteEmail() { return solicitanteEmail; }
    public void setSolicitanteEmail(String solicitanteEmail) { this.solicitanteEmail = solicitanteEmail; }

    public String getSolicitanteNombre() { return solicitanteNombre; }
    public void setSolicitanteNombre(String solicitanteNombre) { this.solicitanteNombre = solicitanteNombre; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
