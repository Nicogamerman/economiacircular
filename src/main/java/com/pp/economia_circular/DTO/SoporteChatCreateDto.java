package com.pp.economia_circular.DTO;

import com.pp.economia_circular.entity.SoporteChat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SoporteChatCreateDto {

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 200, message = "El asunto no puede superar 200 caracteres")
    private String asunto;

    @NotBlank(message = "El mensaje inicial es obligatorio")
    @Size(max = 2000, message = "El mensaje inicial no puede superar 2000 caracteres")
    private String mensajeInicial;

    private SoporteChat.PrioridadChat prioridad = SoporteChat.PrioridadChat.MEDIA;

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getMensajeInicial() { return mensajeInicial; }
    public void setMensajeInicial(String mensajeInicial) { this.mensajeInicial = mensajeInicial; }

    public SoporteChat.PrioridadChat getPrioridad() { return prioridad; }
    public void setPrioridad(SoporteChat.PrioridadChat prioridad) { this.prioridad = prioridad; }
}
