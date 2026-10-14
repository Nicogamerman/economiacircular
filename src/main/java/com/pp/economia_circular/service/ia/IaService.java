package com.pp.economia_circular.service.ia;

import com.pp.economia_circular.DTO.PoliticaResponseDto;
import com.pp.economia_circular.DTO.ia.MensajeChatDto;
import com.pp.economia_circular.DTO.ia.SugerenciaArticuloDto;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.service.JWTService;
import com.pp.economia_circular.service.PoliticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Sugerencias de publicación a partir de una foto y chatbot de ayuda. */
@Service
public class IaService {

    static final List<String> TIPOS_IMAGEN = Arrays.asList("image/jpeg", "image/png", "image/webp", "image/gif");
    static final long MAX_IMAGEN_BYTES = 5L * 1024 * 1024;
    static final int MAX_TURNOS_CHAT = 20;
    static final int MAX_CARACTERES_MENSAJE = 2000;

    static final String PROMPT_CHATBOT =
            "Sos el asistente virtual de Economía Circular, una plataforma de Córdoba (Argentina) para intercambiar, "
                    + "donar o vender artículos usados, encontrar centros de reciclaje, talleres de reparación y "
                    + "eventos o ferias de economía circular. Respondé en español rioplatense, de forma breve y amable.\n\n"
                    + "Cómo funciona la plataforma:\n"
                    + "- Publicar: desde 'Mis artículos' se carga título, descripción, categoría, subcategoría, marca, "
                    + "etiquetas, hasta 5 fotos y un video. Un administrador aprueba la publicación antes de que sea visible. "
                    + "La IA puede sugerir los datos a partir de una foto.\n"
                    + "- Intercambiar: en el detalle de un artículo se elige 'Solicitar intercambio' y se ofrece un artículo "
                    + "propio. El dueño acepta o rechaza; al concretarlo lo marca como completado. En 'Intercambios' están "
                    + "las solicitudes enviadas, recibidas y el historial.\n"
                    + "- Devoluciones: después de completar un intercambio, cualquiera de las partes puede pedir la "
                    + "devolución dentro del plazo indicado en las políticas; la otra parte la acepta o la rechaza.\n"
                    + "- Chat: con 'Contactar' se escribe al oferente; también se pueden mandar mensajes de voz.\n"
                    + "- Favoritos, seguir oferentes y eventos, y elegir tipos de evento de interés para recibir avisos.\n"
                    + "- Valoraciones: después de un intercambio completado se puede valorar a la otra persona.\n"
                    + "- Cuenta: en Configuración se edita el perfil, la foto, la contraseña, se verifica el celular y la "
                    + "identidad (documento + selfie), que muestra una insignia de usuario verificado.\n"
                    + "- Soporte: para problemas con una cuenta, una publicación o un intercambio puntual, el usuario "
                    + "debe abrir una consulta en 'Soporte', donde responde una persona del equipo.\n\n"
                    + "Reglas: respondé solo sobre la plataforma y la economía circular. No inventes funciones, precios "
                    + "ni plazos que no estén acá o en las políticas. No pidas contraseñas ni datos de documentos. "
                    + "Si no sabés algo o el caso requiere revisar una cuenta, sugerí abrir una consulta en Soporte.";

    @Autowired
    private IaClient iaClient;

    @Autowired
    private LimitadorIa limitador;

    @Autowired
    private JWTService authService;

    @Autowired(required = false)
    private PoliticaService politicaService;

    public boolean disponible() {
        return iaClient.disponible();
    }

    public SugerenciaArticuloDto sugerirArticulo(MultipartFile imagen) {
        Usuario usuario = requerirUsuario();
        validarImagen(imagen);
        requerirIa();
        limitador.registrar(usuario.getId());
        try {
            return iaClient.sugerirArticulo(imagen.getBytes(), imagen.getContentType());
        } catch (IOException e) {
            throw new IaException("No se pudo leer la imagen");
        }
    }

    public String chat(List<MensajeChatDto> historial) {
        Usuario usuario = requerirUsuario();
        List<MensajeChatDto> mensajes = normalizarHistorial(historial);
        requerirIa();
        limitador.registrar(usuario.getId());
        return iaClient.responderChat(construirPromptChatbot(), mensajes);
    }

    /** Deja los últimos turnos válidos, empezando por un mensaje del usuario y terminando con uno. */
    static List<MensajeChatDto> normalizarHistorial(List<MensajeChatDto> historial) {
        if (historial == null || historial.isEmpty()) {
            throw new IaException("Escribí una pregunta");
        }
        List<MensajeChatDto> validos = new ArrayList<>();
        for (MensajeChatDto m : historial) {
            if (m == null || m.getContenido() == null || m.getContenido().trim().isEmpty()) continue;
            String rol = "assistant".equals(m.getRol()) ? "assistant" : "user";
            String contenido = m.getContenido().trim();
            if (contenido.length() > MAX_CARACTERES_MENSAJE) {
                contenido = contenido.substring(0, MAX_CARACTERES_MENSAJE);
            }
            // Turnos consecutivos del mismo rol se unen para respetar la alternancia
            if (!validos.isEmpty() && validos.get(validos.size() - 1).getRol().equals(rol)) {
                MensajeChatDto anterior = validos.get(validos.size() - 1);
                anterior.setContenido(anterior.getContenido() + "\n" + contenido);
            } else {
                validos.add(new MensajeChatDto(rol, contenido));
            }
        }
        if (validos.isEmpty() || !"user".equals(validos.get(validos.size() - 1).getRol())) {
            throw new IaException("El último mensaje tiene que ser una pregunta del usuario");
        }
        if (validos.size() > MAX_TURNOS_CHAT) {
            validos = new ArrayList<>(validos.subList(validos.size() - MAX_TURNOS_CHAT, validos.size()));
        }
        while (!validos.isEmpty() && !"user".equals(validos.get(0).getRol())) {
            validos.remove(0);
        }
        return validos;
    }

    String construirPromptChatbot() {
        StringBuilder sb = new StringBuilder(PROMPT_CHATBOT);
        if (politicaService != null) {
            List<PoliticaResponseDto> politicas = politicaService.listarActivas(null);
            if (!politicas.isEmpty()) {
                sb.append("\n\nPolíticas y reglas vigentes de la plataforma:\n");
                for (PoliticaResponseDto p : politicas) {
                    sb.append("\n## ").append(p.getTitulo()).append("\n").append(p.getContenido()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public static void validarImagen(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) throw new IaException("Subí una imagen");
        if (imagen.getContentType() == null || !TIPOS_IMAGEN.contains(imagen.getContentType())) {
            throw new IaException("Formato de imagen no soportado. Usá JPG, PNG, WEBP o GIF.");
        }
        if (imagen.getSize() > MAX_IMAGEN_BYTES) throw new IaException("La imagen no puede superar 5 MB");
    }

    private void requerirIa() {
        if (!iaClient.disponible()) {
            throw new IaException("La IA no está configurada en el servidor", true);
        }
    }

    private Usuario requerirUsuario() {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new IaException("Usuario no autenticado");
        return usuario;
    }
}
