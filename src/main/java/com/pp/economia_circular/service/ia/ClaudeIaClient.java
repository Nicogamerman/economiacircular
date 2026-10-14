package com.pp.economia_circular.service.ia;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.models.messages.*;
import com.pp.economia_circular.DTO.ia.AnalisisIdentidadDto;
import com.pp.economia_circular.DTO.ia.MensajeChatDto;
import com.pp.economia_circular.DTO.ia.SugerenciaArticuloDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Implementación de IaClient con el SDK oficial de Claude. */
@Component
public class ClaudeIaClient implements IaClient {

    private static final Logger log = LoggerFactory.getLogger(ClaudeIaClient.class);

    private static final String PROMPT_SUGERENCIA =
            "Sos un asistente de una plataforma argentina de economía circular donde la gente intercambia, "
                    + "dona o vende artículos usados. Mirá la foto y completá los datos de la publicación. "
                    + "Describí solo lo que se ve; si no distinguís la marca o el modelo, dejalos vacíos. "
                    + "Si la foto no muestra un artículo reconocible, usá la categoría OTROS y una confianza baja.";

    private static final String PROMPT_IDENTIDAD =
            "Revisás pedidos de verificación de identidad de una plataforma de intercambio de artículos usados, "
                    + "para prevenir cuentas falsas. Recibís dos imágenes: (1) la foto de un documento de identidad y "
                    + "(2) una selfie de la persona sosteniendo ese documento. No compares rasgos faciales: evaluá si el "
                    + "documento parece auténtico y legible, si el nombre impreso coincide con el nombre declarado, si la "
                    + "selfie muestra a una persona real sosteniendo el mismo documento, y si hay indicios de edición, "
                    + "capturas de pantalla o fotos de una pantalla. Ante la duda, elegí REVISION_MANUAL. "
                    + "En el motivo no repitas el número de documento ni otros datos personales.";

    private final String apiKey;
    private final String modelo;
    private volatile AnthropicClient client;

    public ClaudeIaClient(@Value("${app.ia.api-key:}") String apiKey,
                          @Value("${app.ia.model:claude-opus-5-5}") String modelo) {
        this.apiKey = apiKey;
        this.modelo = modelo;
    }

    @Override
    public boolean disponible() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    private AnthropicClient cliente() {
        if (!disponible()) {
            throw new IaException("La IA no está configurada en el servidor", true);
        }
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
                }
            }
        }
        return client;
    }

    @Override
    public SugerenciaArticuloDto sugerirArticulo(byte[] imagen, String mediaType) {
        StructuredMessageCreateParams<SugerenciaArticuloDto> params = MessageCreateParams.builder()
                .model(modelo)
                .maxTokens(16000L)
                .system(PROMPT_SUGERENCIA)
                .outputConfig(SugerenciaArticuloDto.class)
                .addUserMessageOfBlockParams(listaDe(
                        imagen(imagen, mediaType),
                        texto("Sugerí los datos de la publicación para este artículo.")))
                .build();
        return primerResultado(ejecutar(params));
    }

    @Override
    public AnalisisIdentidadDto analizarIdentidad(byte[] documento, String documentoMediaType,
                                                  byte[] selfie, String selfieMediaType,
                                                  String nombreDeclarado) {
        StructuredMessageCreateParams<AnalisisIdentidadDto> params = MessageCreateParams.builder()
                .model(modelo)
                .maxTokens(16000L)
                .system(PROMPT_IDENTIDAD)
                .outputConfig(AnalisisIdentidadDto.class)
                .addUserMessageOfBlockParams(listaDe(
                        texto("Imagen 1: documento de identidad."),
                        imagen(documento, documentoMediaType),
                        texto("Imagen 2: selfie con el documento en la mano."),
                        imagen(selfie, selfieMediaType),
                        texto("Nombre declarado en el perfil: " + nombreDeclarado)))
                .build();
        return primerResultado(ejecutar(params));
    }

    @Override
    public String responderChat(String systemPrompt, List<MensajeChatDto> historial) {
        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(modelo)
                .maxTokens(4000L)
                .system(systemPrompt)
                // Respuestas cortas de asistencia: poco razonamiento alcanza y baja la latencia
                .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.LOW).build());
        for (MensajeChatDto m : historial) {
            if ("assistant".equals(m.getRol())) {
                builder.addAssistantMessage(m.getContenido());
            } else {
                builder.addUserMessage(m.getContenido());
            }
        }

        Message respuesta;
        try {
            respuesta = cliente().messages().create(builder.build());
        } catch (RuntimeException e) {
            throw traducir(e);
        }
        verificarRechazo(respuesta.stopReason());
        String texto = respuesta.content().stream()
                .flatMap(bloque -> bloque.text().map(TextBlock::text).map(java.util.stream.Stream::of)
                        .orElseGet(java.util.stream.Stream::empty))
                .collect(Collectors.joining("\n"))
                .trim();
        if (texto.isEmpty()) {
            throw new IaException("El asistente no generó una respuesta. Probá reformular la pregunta.");
        }
        return texto;
    }

    // ---------- helpers ----------

    private <T> StructuredMessage<T> ejecutar(StructuredMessageCreateParams<T> params) {
        StructuredMessage<T> respuesta;
        try {
            respuesta = cliente().messages().create(params);
        } catch (RuntimeException e) {
            throw traducir(e);
        }
        verificarRechazo(respuesta.stopReason());
        return respuesta;
    }

    private <T> T primerResultado(StructuredMessage<T> respuesta) {
        for (StructuredContentBlock<T> bloque : respuesta.content()) {
            Optional<StructuredTextBlock<T>> texto = bloque.text();
            if (texto.isPresent()) {
                return texto.get().text();
            }
        }
        throw new IaException("La IA no devolvió un resultado. Intentá de nuevo.");
    }

    private void verificarRechazo(Optional<StopReason> stopReason) {
        if (stopReason.isPresent() && StopReason.REFUSAL.equals(stopReason.get())) {
            throw new IaException("La IA no pudo procesar este pedido.");
        }
        if (stopReason.isPresent() && StopReason.MAX_TOKENS.equals(stopReason.get())) {
            throw new IaException("La respuesta de la IA quedó incompleta. Intentá de nuevo.");
        }
    }

    private IaException traducir(RuntimeException e) {
        if (e instanceof IaException) return (IaException) e;
        if (e instanceof RateLimitException) {
            return new IaException("La IA está recibiendo muchos pedidos. Probá en unos minutos.", true);
        }
        if (e instanceof AnthropicServiceException) {
            log.warn("Error de la API de Claude: {}", e.getMessage());
            return new IaException("El servicio de IA no respondió correctamente.", true);
        }
        log.warn("No se pudo contactar a la API de Claude", e);
        return new IaException("No se pudo contactar al servicio de IA.", true);
    }

    private static List<ContentBlockParam> listaDe(ContentBlockParam... bloques) {
        List<ContentBlockParam> lista = new ArrayList<>();
        for (ContentBlockParam b : bloques) lista.add(b);
        return lista;
    }

    private static ContentBlockParam texto(String texto) {
        return ContentBlockParam.ofText(TextBlockParam.builder().text(texto).build());
    }

    private static ContentBlockParam imagen(byte[] datos, String mediaType) {
        return ContentBlockParam.ofImage(ImageBlockParam.builder()
                .source(Base64ImageSource.builder()
                        .data(Base64.getEncoder().encodeToString(datos))
                        .mediaType(mediaTypeImagen(mediaType))
                        .build())
                .build());
    }

    static Base64ImageSource.MediaType mediaTypeImagen(String mediaType) {
        if ("image/png".equals(mediaType)) return Base64ImageSource.MediaType.IMAGE_PNG;
        if ("image/webp".equals(mediaType)) return Base64ImageSource.MediaType.IMAGE_WEBP;
        if ("image/gif".equals(mediaType)) return Base64ImageSource.MediaType.IMAGE_GIF;
        if ("image/jpeg".equals(mediaType) || "image/jpg".equals(mediaType)) return Base64ImageSource.MediaType.IMAGE_JPEG;
        throw new IaException("Formato de imagen no soportado. Usá JPG, PNG, WEBP o GIF.");
    }
}
