package com.pp.economia_circular.DTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class MensajeSoporteEnviarDto {

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(max = 2000, message = "El mensaje no puede superar 2000 caracteres")
    private String contenido;

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}
