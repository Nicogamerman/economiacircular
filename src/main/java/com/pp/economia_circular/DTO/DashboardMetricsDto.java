package com.pp.economia_circular.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardMetricsDto {

    private long totalUsuarios;
    private long usuariosActivos;
    private long usuariosNuevosUltimos30Dias;

    private long totalArticulos;
    private long articulosDisponibles;
    private long articulosIntercambiados;
    private long articulosUltimos30Dias;

    private long totalSolicitudes;
    private long intercambiosCompletados;
    private long solicitudesPendientes;

    private long totalVistas;
    private long vistasUltimos30Dias;

    private long totalMensajes;
    private long mensajesUltimos30Dias;
}
