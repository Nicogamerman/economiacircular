package com.pp.economia_circular.DTO;

public class TimelinePointDto {

    private String periodo;
    private long valor;

    public TimelinePointDto() {}

    public TimelinePointDto(String periodo, long valor) {
        this.periodo = periodo;
        this.valor = valor;
    }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public long getValor() { return valor; }
    public void setValor(long valor) { this.valor = valor; }
}
