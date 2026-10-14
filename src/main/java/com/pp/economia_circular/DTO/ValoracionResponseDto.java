package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.Valoracion;

import java.time.LocalDateTime;

public class ValoracionResponseDto {

    private Long id;
    private Integer puntaje;
    private String comentario;
    private Long valoradorId;
    private String valoradorNombre;
    private String valoradorEmail;
    private Long valoradoId;
    private String valoradoNombre;
    private Long articuloId;
    private String articuloTitulo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public ValoracionResponseDto() {}

    public ValoracionResponseDto(Valoracion v) {
        this.id = v.getId();
        this.puntaje = v.getPuntaje();
        this.comentario = v.getComentario();
        this.creadoEn = v.getCreadoEn();
        this.actualizadoEn = v.getActualizadoEn();

        if (v.getValorador() != null) {
            this.valoradorId = v.getValorador().getId();
            this.valoradorEmail = v.getValorador().getEmail();
            this.valoradorNombre = construirNombre(v.getValorador().getNombre(), v.getValorador().getApellido());
        }
        if (v.getValorado() != null) {
            this.valoradoId = v.getValorado().getId();
            this.valoradoNombre = construirNombre(v.getValorado().getNombre(), v.getValorado().getApellido());
        }
        if (v.getArticulo() != null) {
            this.articuloId = v.getArticulo().getId();
            this.articuloTitulo = v.getArticulo().getTitulo();
        }
    }

    private String construirNombre(String nombre, String apellido) {
        String n = nombre != null ? nombre : "";
        String a = apellido != null ? apellido : "";
        String combinado = (n + " " + a).trim();
        return combinado.isEmpty() ? null : combinado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPuntaje() { return puntaje; }
    public void setPuntaje(Integer puntaje) { this.puntaje = puntaje; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Long getValoradorId() { return valoradorId; }
    public void setValoradorId(Long valoradorId) { this.valoradorId = valoradorId; }

    public String getValoradorNombre() { return valoradorNombre; }
    public void setValoradorNombre(String valoradorNombre) { this.valoradorNombre = valoradorNombre; }

    public String getValoradorEmail() { return valoradorEmail; }
    public void setValoradorEmail(String valoradorEmail) { this.valoradorEmail = valoradorEmail; }

    public Long getValoradoId() { return valoradoId; }
    public void setValoradoId(Long valoradoId) { this.valoradoId = valoradoId; }

    public String getValoradoNombre() { return valoradoNombre; }
    public void setValoradoNombre(String valoradoNombre) { this.valoradoNombre = valoradoNombre; }

    public Long getArticuloId() { return articuloId; }
    public void setArticuloId(Long articuloId) { this.articuloId = articuloId; }

    public String getArticuloTitulo() { return articuloTitulo; }
    public void setArticuloTitulo(String articuloTitulo) { this.articuloTitulo = articuloTitulo; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
