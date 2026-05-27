package com.pp.economia_circular.DTO;

public class DistributionEntryDto {

    private String label;
    private long cantidad;
    private double porcentaje;

    public DistributionEntryDto() {}

    public DistributionEntryDto(String label, long cantidad, double porcentaje) {
        this.label = label;
        this.cantidad = cantidad;
        this.porcentaje = porcentaje;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public long getCantidad() { return cantidad; }
    public void setCantidad(long cantidad) { this.cantidad = cantidad; }

    public double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }
}
