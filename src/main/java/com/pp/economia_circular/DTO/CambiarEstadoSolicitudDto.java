package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.SolicitudIntercambio;

import javax.validation.constraints.NotNull;

public class CambiarEstadoSolicitudDto {

    @NotNull(message = "El nuevo estado es obligatorio")
    private SolicitudIntercambio.EstadoIntercambio nuevoEstado;

    public SolicitudIntercambio.EstadoIntercambio getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(SolicitudIntercambio.EstadoIntercambio nuevoEstado) { this.nuevoEstado = nuevoEstado; }
}
