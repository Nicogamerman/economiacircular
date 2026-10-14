package com.pp.economia_circular.DTO.ia;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Resultado del análisis de documento + selfie con el documento en la mano.
 * No compara rasgos faciales: revisa que el documento parezca auténtico, que los datos
 * coincidan con el perfil y que la selfie muestre a una persona real sosteniendo ese documento.
 */
public class AnalisisIdentidadDto {

    @JsonPropertyDescription("true si la primera imagen es un documento de identidad legible (DNI, pasaporte o licencia)")
    public Boolean documentoLegible;

    @JsonPropertyDescription("Tipo de documento detectado, por ejemplo 'DNI argentino'. Vacío si no se reconoce")
    public String tipoDocumento;

    @JsonPropertyDescription("true si el nombre impreso en el documento coincide razonablemente con el nombre declarado")
    public Boolean nombreCoincide;

    @JsonPropertyDescription("true si la segunda imagen muestra a una persona real sosteniendo el mismo documento")
    public Boolean selfieConDocumento;

    @JsonPropertyDescription("true si hay indicios de edición, captura de pantalla, foto de una pantalla o documento falso")
    public Boolean indiciosDeFraude;

    @JsonPropertyDescription("Confianza global del análisis, de 0 a 100")
    public Integer confianza;

    @JsonPropertyDescription("APROBADO si todo es consistente, RECHAZADO si hay indicios claros de fraude o datos que no coinciden, REVISION_MANUAL si las imágenes no alcanzan para decidir")
    public String veredicto;

    @JsonPropertyDescription("Explicación breve en español para el usuario, sin repetir datos personales del documento")
    public String motivo;
}
