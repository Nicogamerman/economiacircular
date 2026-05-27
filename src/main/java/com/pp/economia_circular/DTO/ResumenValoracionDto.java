package com.pp.economia_circular.DTO;

public class ResumenValoracionDto {

    private Long usuarioId;
    private Double promedio;
    private Long totalValoraciones;
    private Long cantidad5;
    private Long cantidad4;
    private Long cantidad3;
    private Long cantidad2;
    private Long cantidad1;

    public ResumenValoracionDto() {}

    public ResumenValoracionDto(Long usuarioId, Double promedio, Long totalValoraciones,
                                Long cantidad5, Long cantidad4, Long cantidad3,
                                Long cantidad2, Long cantidad1) {
        this.usuarioId = usuarioId;
        this.promedio = promedio != null ? Math.round(promedio * 10.0) / 10.0 : 0.0;
        this.totalValoraciones = totalValoraciones != null ? totalValoraciones : 0L;
        this.cantidad5 = cantidad5 != null ? cantidad5 : 0L;
        this.cantidad4 = cantidad4 != null ? cantidad4 : 0L;
        this.cantidad3 = cantidad3 != null ? cantidad3 : 0L;
        this.cantidad2 = cantidad2 != null ? cantidad2 : 0L;
        this.cantidad1 = cantidad1 != null ? cantidad1 : 0L;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Double getPromedio() { return promedio; }
    public void setPromedio(Double promedio) { this.promedio = promedio; }

    public Long getTotalValoraciones() { return totalValoraciones; }
    public void setTotalValoraciones(Long totalValoraciones) { this.totalValoraciones = totalValoraciones; }

    public Long getCantidad5() { return cantidad5; }
    public void setCantidad5(Long cantidad5) { this.cantidad5 = cantidad5; }

    public Long getCantidad4() { return cantidad4; }
    public void setCantidad4(Long cantidad4) { this.cantidad4 = cantidad4; }

    public Long getCantidad3() { return cantidad3; }
    public void setCantidad3(Long cantidad3) { this.cantidad3 = cantidad3; }

    public Long getCantidad2() { return cantidad2; }
    public void setCantidad2(Long cantidad2) { this.cantidad2 = cantidad2; }

    public Long getCantidad1() { return cantidad1; }
    public void setCantidad1(Long cantidad1) { this.cantidad1 = cantidad1; }
}
