package com.pp.economia_circular.DTO;

import javax.validation.constraints.NotNull;

public class SolicitudCreateDto {

    @NotNull(message = "El artículo solicitado es obligatorio")
    private Long articuloSolicitadoId;

    @NotNull(message = "El artículo ofrecido es obligatorio")
    private Long articuloOfrecidoId;

    public Long getArticuloSolicitadoId() { return articuloSolicitadoId; }
    public void setArticuloSolicitadoId(Long articuloSolicitadoId) { this.articuloSolicitadoId = articuloSolicitadoId; }

    public Long getArticuloOfrecidoId() { return articuloOfrecidoId; }
    public void setArticuloOfrecidoId(Long articuloOfrecidoId) { this.articuloOfrecidoId = articuloOfrecidoId; }
}
