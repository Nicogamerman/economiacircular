package com.pp.economia_circular.DTO.ia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Un turno del chatbot: rol "user" o "assistant". */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MensajeChatDto {
    private String rol;
    private String contenido;
}
