package com.pp.economia_circular.DTO;

import java.time.LocalDateTime;

public class DashboardSummaryDto {

    private LocalDateTime generatedAt;

    private long usuariosTotales;
    private long usuariosActivos;
    private long usuariosNuevosUltimos30Dias;
    private double crecimientoUsuariosPctMesActualVsAnterior;

    private long articulosTotales;
    private long articulosDisponibles;
    private long articulosPublicadosUltimos30Dias;
    private double crecimientoArticulosPctMesActualVsAnterior;

    private long intercambiosTotales;
    private long intercambiosPendientes;
    private long intercambiosCompletados;
    private long intercambiosUltimos30Dias;

    private long eventosTotales;
    private long eventosActivos;
    private long centrosReciclajeTotales;
    private long centrosReciclajeActivos;

    private long mensajesTotales;
    private long valoracionesTotales;
    private Double promedioValoraciones;

    public DashboardSummaryDto() {
        this.generatedAt = LocalDateTime.now();
    }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public long getUsuariosTotales() { return usuariosTotales; }
    public void setUsuariosTotales(long usuariosTotales) { this.usuariosTotales = usuariosTotales; }

    public long getUsuariosActivos() { return usuariosActivos; }
    public void setUsuariosActivos(long usuariosActivos) { this.usuariosActivos = usuariosActivos; }

    public long getUsuariosNuevosUltimos30Dias() { return usuariosNuevosUltimos30Dias; }
    public void setUsuariosNuevosUltimos30Dias(long v) { this.usuariosNuevosUltimos30Dias = v; }

    public double getCrecimientoUsuariosPctMesActualVsAnterior() { return crecimientoUsuariosPctMesActualVsAnterior; }
    public void setCrecimientoUsuariosPctMesActualVsAnterior(double v) { this.crecimientoUsuariosPctMesActualVsAnterior = v; }

    public long getArticulosTotales() { return articulosTotales; }
    public void setArticulosTotales(long articulosTotales) { this.articulosTotales = articulosTotales; }

    public long getArticulosDisponibles() { return articulosDisponibles; }
    public void setArticulosDisponibles(long v) { this.articulosDisponibles = v; }

    public long getArticulosPublicadosUltimos30Dias() { return articulosPublicadosUltimos30Dias; }
    public void setArticulosPublicadosUltimos30Dias(long v) { this.articulosPublicadosUltimos30Dias = v; }

    public double getCrecimientoArticulosPctMesActualVsAnterior() { return crecimientoArticulosPctMesActualVsAnterior; }
    public void setCrecimientoArticulosPctMesActualVsAnterior(double v) { this.crecimientoArticulosPctMesActualVsAnterior = v; }

    public long getIntercambiosTotales() { return intercambiosTotales; }
    public void setIntercambiosTotales(long v) { this.intercambiosTotales = v; }

    public long getIntercambiosPendientes() { return intercambiosPendientes; }
    public void setIntercambiosPendientes(long v) { this.intercambiosPendientes = v; }

    public long getIntercambiosCompletados() { return intercambiosCompletados; }
    public void setIntercambiosCompletados(long v) { this.intercambiosCompletados = v; }

    public long getIntercambiosUltimos30Dias() { return intercambiosUltimos30Dias; }
    public void setIntercambiosUltimos30Dias(long v) { this.intercambiosUltimos30Dias = v; }

    public long getEventosTotales() { return eventosTotales; }
    public void setEventosTotales(long v) { this.eventosTotales = v; }

    public long getEventosActivos() { return eventosActivos; }
    public void setEventosActivos(long v) { this.eventosActivos = v; }

    public long getCentrosReciclajeTotales() { return centrosReciclajeTotales; }
    public void setCentrosReciclajeTotales(long v) { this.centrosReciclajeTotales = v; }

    public long getCentrosReciclajeActivos() { return centrosReciclajeActivos; }
    public void setCentrosReciclajeActivos(long v) { this.centrosReciclajeActivos = v; }

    public long getMensajesTotales() { return mensajesTotales; }
    public void setMensajesTotales(long v) { this.mensajesTotales = v; }

    public long getValoracionesTotales() { return valoracionesTotales; }
    public void setValoracionesTotales(long v) { this.valoracionesTotales = v; }

    public Double getPromedioValoraciones() { return promedioValoraciones; }
    public void setPromedioValoraciones(Double v) { this.promedioValoraciones = v; }
}
