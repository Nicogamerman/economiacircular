package com.pp.economia_circular.DTO;

import javax.validation.constraints.NotBlank;

public class MensajeSoporteEnviarDto {

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    private String contenido;

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}
