package com.pp.economia_circular.service.ia;

import com.pp.economia_circular.DTO.ia.AnalisisIdentidadDto;
import com.pp.economia_circular.DTO.ia.MensajeChatDto;
import com.pp.economia_circular.DTO.ia.SugerenciaArticuloDto;

import java.util.List;

/**
 * Operaciones de IA que usa la aplicación. Los servicios dependen de esta interfaz
 * (y no del SDK) para poder probarlos sin llamar a la API.
 */
public interface IaClient {

    /** true si hay una API key configurada. */
    boolean disponible();

    SugerenciaArticuloDto sugerirArticulo(byte[] imagen, String mediaType);

    AnalisisIdentidadDto analizarIdentidad(byte[] documento, String documentoMediaType,
                                           byte[] selfie, String selfieMediaType,
                                           String nombreDeclarado);

    String responderChat(String systemPrompt, List<MensajeChatDto> historial);
}
