package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.Devolucion;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
public class DevolucionDto {

    private Long id;
    private Long solicitudId;
    private Long solicitanteId;
    private String solicitanteEmail;
    private String motivo;
    private Devolucion.EstadoDevolucion estado;
    private String respuesta;
    private String articuloSolicitadoTitulo;
    private String articuloOfrecidoTitulo;
    private LocalDateTime creadoEn;
    private LocalDateTime resueltoEn;

    public static DevolucionDto from(Devolucion d) {
        DevolucionDto dto = new DevolucionDto();
        dto.setId(d.getId());
        dto.setMotivo(d.getMotivo());
        dto.setEstado(d.getEstado());
        dto.setRespuesta(d.getRespuesta());
        dto.setCreadoEn(d.getCreadoEn());
        dto.setResueltoEn(d.getResueltoEn());
        if (d.getSolicitante() != null) {
            dto.setSolicitanteId(d.getSolicitante().getId());
            dto.setSolicitanteEmail(d.getSolicitante().getEmail());
        }
        if (d.getSolicitud() != null) {
            dto.setSolicitudId(d.getSolicitud().getId());
            if (d.getSolicitud().getArticuloSolicitado() != null) {
                dto.setArticuloSolicitadoTitulo(d.getSolicitud().getArticuloSolicitado().getTitulo());
            }
            if (d.getSolicitud().getArticuloOfrecido() != null) {
                dto.setArticuloOfrecidoTitulo(d.getSolicitud().getArticuloOfrecido().getTitulo());
            }
        }
        return dto;
    }

    /** Cuerpo para pedir una devolución. */
    @Getter
    @Setter
    public static class Crear {
        @NotBlank(message = "Contanos el motivo de la devolución")
        @Size(max = 1000)
        private String motivo;
    }

    /** Cuerpo para aceptar o rechazar una devolución. */
    @Getter
    @Setter
    public static class Responder {
        private boolean aceptar;
        @Size(max = 1000)
        private String respuesta;
    }
}
