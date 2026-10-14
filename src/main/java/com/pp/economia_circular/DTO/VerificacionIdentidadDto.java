package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.VerificacionIdentidad;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VerificacionIdentidadDto {

    private Long id;
    private Long usuarioId;
    private String usuarioEmail;
    private String usuarioNombre;
    private VerificacionIdentidad.EstadoVerificacion estado;
    private String tipoDocumento;
    private Integer confianza;
    private String motivo;
    private String notaRevision;
    private LocalDateTime creadoEn;
    private LocalDateTime resueltoEn;

    public static VerificacionIdentidadDto from(VerificacionIdentidad v) {
        VerificacionIdentidadDto dto = new VerificacionIdentidadDto();
        dto.setId(v.getId());
        dto.setEstado(v.getEstado());
        dto.setTipoDocumento(v.getTipoDocumento());
        dto.setConfianza(v.getConfianza());
        dto.setMotivo(v.getMotivo());
        dto.setNotaRevision(v.getNotaRevision());
        dto.setCreadoEn(v.getCreadoEn());
        dto.setResueltoEn(v.getResueltoEn());
        if (v.getUsuario() != null) {
            dto.setUsuarioId(v.getUsuario().getId());
            dto.setUsuarioEmail(v.getUsuario().getEmail());
            String nombre = ((v.getUsuario().getNombre() != null ? v.getUsuario().getNombre() : "") + " "
                    + (v.getUsuario().getApellido() != null ? v.getUsuario().getApellido() : "")).trim();
            dto.setUsuarioNombre(nombre);
        }
        return dto;
    }
}
